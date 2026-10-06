package com.queuesense.service;

import com.queuesense.dao.*;
import com.queuesense.dto.QueueItemDto;
import com.queuesense.engine.*;
import com.queuesense.enums.CounterStatus;
import com.queuesense.enums.PauseReason;
import com.queuesense.enums.PriorityType;
import com.queuesense.enums.TokenStatus;
import com.queuesense.enums.TransferReason;
import com.queuesense.exception.*;
import com.queuesense.model.*;
import com.queuesense.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * QueueService - Orchestrates queue lifecycle, concurrency-safe counter assignments,
 * dynamic PriorityQueue evaluations, and service sessions.
 */
public class QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);

    private final TokenDao tokenDao;
    private final CustomerDao customerDao;
    private final ServiceDao serviceDao;
    private final CounterDao counterDao;
    private final TokenStatusHistoryDao statusHistoryDao;
    private final ServiceSessionDao sessionDao;
    private final TokenTransferDao transferDao;
    private final FeedbackDao feedbackDao;
    private final RuleDao ruleDao;
    private final AuditLogDao auditLogDao;

    private final QueueDecisionEngine queueDecisionEngine;
    private final CounterAllocationEngine counterAllocationEngine;
    private final WaitingTimeEstimator waitingTimeEstimator;

    public QueueService() {
        this.tokenDao = new TokenDao();
        this.customerDao = new CustomerDao();
        this.serviceDao = new ServiceDao();
        this.counterDao = new CounterDao();
        this.statusHistoryDao = new TokenStatusHistoryDao();
        this.sessionDao = new ServiceSessionDao();
        this.transferDao = new TokenTransferDao();
        this.feedbackDao = new FeedbackDao();
        this.ruleDao = new RuleDao();
        this.auditLogDao = new AuditLogDao();

        this.queueDecisionEngine = new QueueDecisionEngine();
        this.counterAllocationEngine = new CounterAllocationEngine();
        this.waitingTimeEstimator = new WaitingTimeEstimator();
    }

    public QueueService(TokenDao tokenDao, CustomerDao customerDao, ServiceDao serviceDao,
                        CounterDao counterDao, TokenStatusHistoryDao statusHistoryDao,
                        ServiceSessionDao sessionDao, TokenTransferDao transferDao,
                        FeedbackDao feedbackDao, RuleDao ruleDao, AuditLogDao auditLogDao,
                        QueueDecisionEngine queueDecisionEngine,
                        CounterAllocationEngine counterAllocationEngine,
                        WaitingTimeEstimator waitingTimeEstimator) {
        this.tokenDao = tokenDao;
        this.customerDao = customerDao;
        this.serviceDao = serviceDao;
        this.counterDao = counterDao;
        this.statusHistoryDao = statusHistoryDao;
        this.sessionDao = sessionDao;
        this.transferDao = transferDao;
        this.feedbackDao = feedbackDao;
        this.ruleDao = ruleDao;
        this.auditLogDao = auditLogDao;
        this.queueDecisionEngine = queueDecisionEngine;
        this.counterAllocationEngine = counterAllocationEngine;
        this.waitingTimeEstimator = waitingTimeEstimator;
    }

    // -------------------------------------------------------------
    // 1. TOKEN CREATION
    // -------------------------------------------------------------
    public Token createToken(Long customerId, Long serviceId, PriorityType priorityType, Long createdByUserId) {
        Customer customer = customerDao.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        Service service = serviceDao.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service with ID " + serviceId + " not found."));

        if (!service.isActive()) {
            throw new IllegalArgumentException("Requested service '" + service.getServiceName() + "' is currently inactive.");
        }

        AgingRule agingRule = ruleDao.getActiveAgingRule();
        LocalDateTime now = LocalDateTime.now();

        Token token = new Token();
        token.setCustomerId(customer.getCustomerId());
        token.setCustomerName(customer.getFullName());
        token.setCustomerNumber(customer.getCustomerNumber());
        token.setSpecialAssistanceRequired(customer.isSpecialAssistanceRequired());

        token.setServiceId(service.getServiceId());
        token.setServiceName(service.getServiceName());
        token.setServiceCode(service.getServiceCode());
        token.setServiceAverageMinutes(service.getAverageServiceMinutes());
        token.setServicePriorityWeight(service.getPriorityWeight());

        token.setPriorityType(priorityType != null ? priorityType : PriorityType.NORMAL);
        token.setStatus(TokenStatus.WAITING);
        token.setIssuedAt(now);

        // Calculate initial dynamic score
        double initialScore = PriorityCalculator.calculateScore(token, now, agingRule);
        token.setPriorityScore(initialScore);

        // Calculate dynamic waiting estimate
        List<Token> waitingTokens = tokenDao.findWaitingTokens();
        List<Counter> compatibleCounters = counterDao.findCompatibleCountersForService(service.getServiceId());
        double histAvg = sessionDao.getHistoricalAverageServiceMinutes(service.getServiceId());
        int estimatedMinutes = waitingTimeEstimator.estimateWaitMinutes(token, waitingTokens.size(), compatibleCounters, histAvg);
        token.setEstimatedWaitMinutes(estimatedMinutes);

        token.setTokenNumber(tokenDao.generateNextTokenNumber(service.getServiceCode()));

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            token = tokenDao.create(token, conn);

            TokenStatusHistory history = new TokenStatusHistory(token.getTokenId(), null, TokenStatus.WAITING,
                    createdByUserId, "Token issued at reception/online request");
            statusHistoryDao.insertHistory(history, conn);

            auditLogDao.insertLog(new AuditLog(createdByUserId, "CREATE_TOKEN", "TOKEN",
                    String.valueOf(token.getTokenId()), "Token " + token.getTokenNumber() + " issued for service " + service.getServiceName(), null), conn);

            conn.commit();
            logger.info("Successfully created Token {} for Customer {} (ID: {})",
                    token.getTokenNumber(), customer.getFullName(), token.getTokenId());
            return token;
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Failed to create token", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    // -------------------------------------------------------------
    // 2. CALL NEXT TOKEN (CONCURRENCY-SAFE TRANSACTION)
    // -------------------------------------------------------------
    public Token callNextToken(Long counterId, Long agentId) {
        Counter counter = counterDao.findById(counterId)
                .orElseThrow(() -> new CounterNotFoundException("Counter ID " + counterId + " not found."));

        if (!counter.getStatus().isOperable()) {
            throw new CounterUnavailableException("Counter " + counter.getCounterName() + " is currently " + counter.getStatus());
        }

        // Check if counter is already serving an active customer
        Optional<Token> existingServing = tokenDao.findCurrentServingTokenForCounter(counterId);
        if (existingServing.isPresent()) {
            throw new InvalidTokenStatusException("Counter is already serving active token: " + existingServing.get().getTokenNumber());
        }

        List<Token> waitingTokens = tokenDao.findWaitingTokens();
        if (waitingTokens.isEmpty()) {
            throw new QueueEmptyException("No waiting customers currently in queue.");
        }

        AgingRule agingRule = ruleDao.getActiveAgingRule();
        LocalDateTime now = LocalDateTime.now();

        // Queue Decision Engine evaluates waiting tokens using PriorityQueue and custom Comparator
        Optional<Token> candidateOpt = queueDecisionEngine.getNextToken(
                waitingTokens, counterId, counter.getSupportedServiceIds(), now, agingRule);

        if (candidateOpt.isEmpty()) {
            throw new QueueEmptyException("No waiting customers match Counter " + counter.getCounterNumber() + " supported services.");
        }

        Token selectedToken = candidateOpt.get();

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            // Double-call protection: Atomic row-level claim
            boolean claimed = tokenDao.callTokenAtomically(selectedToken.getTokenId(), counterId, conn);
            if (!claimed) {
                conn.rollback();
                logger.warn("Concurrency conflict: Token {} was claimed simultaneously by another counter", selectedToken.getTokenNumber());
                throw new DatabaseException("Token " + selectedToken.getTokenNumber() + " was just called by another counter. Please try again.");
            }

            // Set counter status to BUSY
            counterDao.updateStatus(counterId, CounterStatus.BUSY);

            // Record status history
            TokenStatusHistory history = new TokenStatusHistory(selectedToken.getTokenId(), TokenStatus.WAITING,
                    TokenStatus.CALLED, agentId, "Called to " + counter.getCounterName());
            statusHistoryDao.insertHistory(history, conn);

            // Record audit log
            auditLogDao.insertLog(new AuditLog(agentId, "CALL_TOKEN", "TOKEN",
                    String.valueOf(selectedToken.getTokenId()), "Counter " + counter.getCounterNumber() + " called Token " + selectedToken.getTokenNumber(), null), conn);

            conn.commit();

            selectedToken.setStatus(TokenStatus.CALLED);
            selectedToken.setCounterId(counterId);
            selectedToken.setCounterNumber(counter.getCounterNumber());
            selectedToken.setCounterName(counter.getCounterName());
            selectedToken.setCalledAt(now);

            logger.info("Counter {} successfully claimed and called Token {}", counter.getCounterNumber(), selectedToken.getTokenNumber());
            return selectedToken;
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Failed to call token", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    // -------------------------------------------------------------
    // 3. START SERVICE
    // -------------------------------------------------------------
    public void startService(Long tokenId, Long counterId, Long agentId) {
        Token token = tokenDao.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException(tokenId));

        if (!token.getStatus().canTransitionTo(TokenStatus.SERVING)) {
            throw new InvalidTokenStatusException(token.getStatus(), TokenStatus.SERVING);
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            tokenDao.updateStatus(tokenId, TokenStatus.SERVING, conn);

            ServiceSession session = new ServiceSession(tokenId, counterId);
            sessionDao.startSession(session, conn);

            TokenStatusHistory history = new TokenStatusHistory(tokenId, token.getStatus(), TokenStatus.SERVING,
                    agentId, "Customer arrived and service started");
            statusHistoryDao.insertHistory(history, conn);

            auditLogDao.insertLog(new AuditLog(agentId, "START_SERVICE", "TOKEN",
                    String.valueOf(tokenId), "Service started for Token " + token.getTokenNumber(), null), conn);

            conn.commit();
            logger.info("Service started for Token {}", token.getTokenNumber());
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Failed to start service session", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    // -------------------------------------------------------------
    // 4. PAUSE SERVICE
    // -------------------------------------------------------------
    public void pauseService(Long tokenId, PauseReason reason, String remarks, Long agentId) {
        Token token = tokenDao.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException(tokenId));

        if (!token.getStatus().canTransitionTo(TokenStatus.PAUSED)) {
            throw new InvalidTokenStatusException(token.getStatus(), TokenStatus.PAUSED);
        }

        ServiceSession activeSession = sessionDao.findActiveSessionByTokenId(tokenId)
                .orElseThrow(() -> new InvalidServiceSessionException("No active service session found for token ID: " + tokenId));

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            tokenDao.updateStatus(tokenId, TokenStatus.PAUSED, conn);
            sessionDao.pauseSession(activeSession.getSessionId(), reason != null ? reason : PauseReason.OTHER, remarks, conn);

            TokenStatusHistory history = new TokenStatusHistory(tokenId, token.getStatus(), TokenStatus.PAUSED,
                    agentId, "Service paused: " + (reason != null ? reason.getLabel() : ""));
            statusHistoryDao.insertHistory(history, conn);

            conn.commit();
            logger.info("Service paused for Token {}", token.getTokenNumber());
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Failed to pause service", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    // -------------------------------------------------------------
    // 5. RESUME SERVICE
    // -------------------------------------------------------------
    public void resumeService(Long tokenId, Long agentId) {
        Token token = tokenDao.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException(tokenId));

        if (!token.getStatus().canTransitionTo(TokenStatus.SERVING)) {
            throw new InvalidTokenStatusException(token.getStatus(), TokenStatus.SERVING);
        }

        ServiceSession activeSession = sessionDao.findActiveSessionByTokenId(tokenId)
                .orElseThrow(() -> new InvalidServiceSessionException("No active service session found for token ID: " + tokenId));

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            tokenDao.updateStatus(tokenId, TokenStatus.SERVING, conn);
            sessionDao.resumeSession(activeSession.getSessionId(), conn);

            TokenStatusHistory history = new TokenStatusHistory(tokenId, token.getStatus(), TokenStatus.SERVING,
                    agentId, "Service resumed");
            statusHistoryDao.insertHistory(history, conn);

            conn.commit();
            logger.info("Service resumed for Token {}", token.getTokenNumber());
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Failed to resume service", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    // -------------------------------------------------------------
    // 6. COMPLETE SERVICE
    // -------------------------------------------------------------
    public void completeService(Long tokenId, String remarks, Long agentId) {
        Token token = tokenDao.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException(tokenId));

        if (!token.getStatus().canTransitionTo(TokenStatus.COMPLETED)) {
            throw new InvalidTokenStatusException(token.getStatus(), TokenStatus.COMPLETED);
        }

        ServiceSession activeSession = sessionDao.findActiveSessionByTokenId(tokenId)
                .orElseThrow(() -> new InvalidServiceSessionException("No active service session found for token ID: " + tokenId));

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            tokenDao.updateStatus(tokenId, TokenStatus.COMPLETED, conn);
            sessionDao.completeSession(activeSession.getSessionId(), remarks, conn);

            // Free the counter back to AVAILABLE
            if (token.getCounterId() != null) {
                counterDao.updateStatus(token.getCounterId(), CounterStatus.AVAILABLE);
            }

            TokenStatusHistory history = new TokenStatusHistory(tokenId, token.getStatus(), TokenStatus.COMPLETED,
                    agentId, "Service completed successfully. " + (remarks != null ? remarks : ""));
            statusHistoryDao.insertHistory(history, conn);

            auditLogDao.insertLog(new AuditLog(agentId, "COMPLETE_SERVICE", "TOKEN",
                    String.valueOf(tokenId), "Token " + token.getTokenNumber() + " completed", null), conn);

            conn.commit();
            logger.info("Service completed for Token {}", token.getTokenNumber());
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Failed to complete service", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    // -------------------------------------------------------------
    // 7. MARK NO-SHOW
    // -------------------------------------------------------------
    public void markNoShow(Long tokenId, Long agentId) {
        Token token = tokenDao.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException(tokenId));

        if (!token.getStatus().canTransitionTo(TokenStatus.NO_SHOW)) {
            throw new InvalidTokenStatusException(token.getStatus(), TokenStatus.NO_SHOW);
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            tokenDao.updateStatus(tokenId, TokenStatus.NO_SHOW, conn);

            if (token.getCounterId() != null) {
                counterDao.updateStatus(token.getCounterId(), CounterStatus.AVAILABLE);
            }

            TokenStatusHistory history = new TokenStatusHistory(tokenId, token.getStatus(), TokenStatus.NO_SHOW,
                    agentId, "Customer did not appear after token was called");
            statusHistoryDao.insertHistory(history, conn);

            auditLogDao.insertLog(new AuditLog(agentId, "MARK_NO_SHOW", "TOKEN",
                    String.valueOf(tokenId), "Token " + token.getTokenNumber() + " marked as NO_SHOW", null), conn);

            conn.commit();
            logger.info("Token {} marked NO_SHOW", token.getTokenNumber());
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Failed to mark no-show", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    // -------------------------------------------------------------
    // 8. CANCEL TOKEN
    // -------------------------------------------------------------
    public void cancelToken(Long tokenId, Long userId, String reason) {
        Token token = tokenDao.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException(tokenId));

        if (!token.getStatus().canTransitionTo(TokenStatus.CANCELLED)) {
            throw new InvalidTokenStatusException(token.getStatus(), TokenStatus.CANCELLED);
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            tokenDao.cancelToken(tokenId, userId, reason, conn);

            if (token.getCounterId() != null && token.getStatus() == TokenStatus.CALLED) {
                counterDao.updateStatus(token.getCounterId(), CounterStatus.AVAILABLE);
            }

            TokenStatusHistory history = new TokenStatusHistory(tokenId, token.getStatus(), TokenStatus.CANCELLED,
                    userId, "Token cancelled: " + reason);
            statusHistoryDao.insertHistory(history, conn);

            auditLogDao.insertLog(new AuditLog(userId, "CANCEL_TOKEN", "TOKEN",
                    String.valueOf(tokenId), "Token " + token.getTokenNumber() + " cancelled. Reason: " + reason, null), conn);

            conn.commit();
            logger.info("Token {} cancelled", token.getTokenNumber());
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Failed to cancel token", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    // -------------------------------------------------------------
    // 9. TRANSFER TOKEN
    // -------------------------------------------------------------
    public void transferToken(Long tokenId, Long fromCounterId, Long toCounterId,
                              TransferReason reason, String remarks, Long agentId) {
        Token token = tokenDao.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException(tokenId));

        if (!token.getStatus().canTransitionTo(TokenStatus.TRANSFERRED)) {
            throw new InvalidTokenStatusException(token.getStatus(), TokenStatus.TRANSFERRED);
        }

        Counter targetCounter = counterDao.findById(toCounterId)
                .orElseThrow(() -> new CounterNotFoundException("Target Counter ID " + toCounterId + " not found."));

        if (!targetCounter.supportsService(token.getServiceId())) {
            throw new ServiceNotSupportedException(toCounterId, token.getServiceId());
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            // Close existing service session if any
            Optional<ServiceSession> sessionOpt = sessionDao.findActiveSessionByTokenId(tokenId);
            if (sessionOpt.isPresent()) {
                sessionDao.completeSession(sessionOpt.get().getSessionId(), "Transferred to Counter " + targetCounter.getCounterNumber(), conn);
            }

            // Free source counter
            counterDao.updateStatus(fromCounterId, CounterStatus.AVAILABLE);

            // Re-bind token to target counter in CALLED status
            tokenDao.updateAssignedCounter(tokenId, toCounterId, conn);
            tokenDao.updateStatus(tokenId, TokenStatus.CALLED, conn);
            counterDao.updateStatus(toCounterId, CounterStatus.BUSY);

            // Log transfer record
            TokenTransfer transfer = new TokenTransfer(tokenId, fromCounterId, toCounterId, reason, agentId, remarks);
            transferDao.insertTransfer(transfer, conn);

            TokenStatusHistory history = new TokenStatusHistory(tokenId, token.getStatus(), TokenStatus.TRANSFERRED,
                    agentId, "Transferred from Counter " + fromCounterId + " to Counter " + targetCounter.getCounterNumber() + " (" + reason.getLabel() + ")");
            statusHistoryDao.insertHistory(history, conn);

            auditLogDao.insertLog(new AuditLog(agentId, "TRANSFER_TOKEN", "TOKEN",
                    String.valueOf(tokenId), "Token " + token.getTokenNumber() + " transferred to Counter " + targetCounter.getCounterNumber(), null), conn);

            conn.commit();
            logger.info("Token {} transferred to Counter {}", token.getTokenNumber(), targetCounter.getCounterNumber());
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            throw new DatabaseException("Failed to transfer token", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    // -------------------------------------------------------------
    // 10. LIVE QUEUE & DISPLAY AGGREGATIONS
    // -------------------------------------------------------------
    public List<QueueItemDto> getLiveQueueDisplayItems() {
        List<Token> waitingTokens = tokenDao.findWaitingTokens();
        AgingRule agingRule = ruleDao.getActiveAgingRule();
        LocalDateTime now = LocalDateTime.now();

        List<Token> ranked = queueDecisionEngine.getRankedWaitingTokens(waitingTokens, now, agingRule);
        List<QueueItemDto> items = new ArrayList<>(ranked.size());

        List<Counter> allCounters = counterDao.findAll();

        for (int i = 0; i < ranked.size(); i++) {
            Token t = ranked.get(i);
            QueueItemDto dto = new QueueItemDto();
            dto.setTokenId(t.getTokenId());
            dto.setTokenNumber(t.getTokenNumber());
            dto.setCustomerName(t.getCustomerName());
            dto.setCustomerNumber(t.getCustomerNumber());
            dto.setSpecialAssistance(t.isSpecialAssistanceRequired());
            dto.setServiceId(t.getServiceId());
            dto.setServiceName(t.getServiceName());
            dto.setPriorityType(t.getPriorityType());
            dto.setPriorityScore(t.getPriorityScore());
            dto.setStatus(t.getStatus());
            dto.setPosition(i + 1);
            dto.setPeopleAhead(i);
            dto.setWaitingMinutesSoFar(t.getWaitingMinutesNow(now));
            dto.setIssuedAt(t.getIssuedAt());

            // Dynamically calculate estimated waiting minutes based on active compatible counters
            List<Counter> compatible = allCounters.stream().filter(c -> c.supportsService(t.getServiceId())).toList();
            int estWait = waitingTimeEstimator.estimateWaitMinutes(t, i, compatible, 0.0);
            dto.setEstimatedWaitMinutes(estWait);

            items.add(dto);
        }
        return items;
    }

    public Optional<Token> getTokenById(Long tokenId) {
        return tokenDao.findById(tokenId);
    }

    public List<Token> getActiveTokensForCustomer(Long customerId) {
        return tokenDao.findActiveTokensByCustomerId(customerId);
    }

    public List<Token> getCustomerTokenHistory(Long customerId) {
        return tokenDao.findHistoryByCustomerId(customerId);
    }

    public void submitFeedback(Long tokenId, Long customerId, int rating, String comment) {
        Token token = tokenDao.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException(tokenId));

        if (!token.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Customer can only submit feedback for their own tokens.");
        }
        if (token.getStatus() != TokenStatus.COMPLETED) {
            throw new IllegalArgumentException("Feedback can only be submitted for completed services.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        if (feedbackDao.findByTokenId(tokenId).isPresent()) {
            throw new DuplicateTokenException("Feedback has already been submitted for token " + token.getTokenNumber());
        }

        Feedback fb = new Feedback(tokenId, customerId, rating, comment);
        feedbackDao.insert(fb);
        logger.info("Feedback submitted for token ID {}", tokenId);
    }
}
