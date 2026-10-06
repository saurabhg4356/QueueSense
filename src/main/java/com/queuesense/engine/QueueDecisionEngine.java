package com.queuesense.engine;

import com.queuesense.model.AgingRule;
import com.queuesense.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * QueueDecisionEngine - The Central Algorithmic Brain of QueueSense.
 *
 * Utilizes Java's PriorityQueue with a dynamic custom Comparator to evaluate
 * multi-factor priorities, anti-starvation aging, and counter-service compatibility.
 */
public class QueueDecisionEngine {

    private static final Logger logger = LoggerFactory.getLogger(QueueDecisionEngine.class);
    private final QueueRankingStrategy rankingStrategy;

    public QueueDecisionEngine() {
        this.rankingStrategy = new FairnessAwareRankingStrategy();
    }

    public QueueDecisionEngine(QueueRankingStrategy rankingStrategy) {
        this.rankingStrategy = rankingStrategy != null ? rankingStrategy : new FairnessAwareRankingStrategy();
    }

    /**
     * Builds an authentic Java PriorityQueue using custom Comparator.
     */
    public PriorityQueue<Token> buildPriorityQueue(Collection<Token> tokens, LocalDateTime referenceTime, AgingRule agingRule) {
        Comparator<Token> comparator = rankingStrategy.getComparator(referenceTime, agingRule);
        PriorityQueue<Token> pq = new PriorityQueue<>(Math.max(1, tokens.size()), comparator);

        for (Token t : tokens) {
            if (t != null) {
                // Update transient calculated score for display/logging
                double dynamicScore = rankingStrategy.calculateScore(t, referenceTime, agingRule);
                t.setPriorityScore(dynamicScore);
                pq.offer(t);
            }
        }
        return pq;
    }

    /**
     * Selects the next eligible token from the queue for a specific counter.
     * Respects counter-service compatibility.
     *
     * @param waitingTokens All current waiting tokens
     * @param counterId ID of requesting counter (for logging)
     * @param supportedServiceIds Services enabled for this counter
     * @param referenceTime Current calculation time
     * @param agingRule Configured aging rules
     * @return Next recommended token, or empty Optional if no compatible tokens exist
     */
    public Optional<Token> getNextToken(List<Token> waitingTokens,
                                        Long counterId,
                                        Set<Long> supportedServiceIds,
                                        LocalDateTime referenceTime,
                                        AgingRule agingRule) {
        if (waitingTokens == null || waitingTokens.isEmpty()) {
            logger.debug("No waiting tokens available for counter ID: {}", counterId);
            return Optional.empty();
        }

        PriorityQueue<Token> pq = buildPriorityQueue(waitingTokens, referenceTime, agingRule);

        // Poll tokens in priority order until one matches the counter's compatible service capabilities
        while (!pq.isEmpty()) {
            Token candidate = pq.poll();
            if (isCompatible(candidate, supportedServiceIds)) {
                logger.info("Counter ID {} selected Token {} [Score: {}, Priority: {}, Service: {}]",
                        counterId, candidate.getTokenNumber(), candidate.getPriorityScore(),
                        candidate.getPriorityType(), candidate.getServiceName());
                return Optional.of(candidate);
            }
        }

        logger.debug("No waiting tokens matched compatible services {} for counter ID: {}", supportedServiceIds, counterId);
        return Optional.empty();
    }

    /**
     * Returns a fully ranked list of all waiting tokens in priority order.
     */
    public List<Token> getRankedWaitingTokens(List<Token> waitingTokens, LocalDateTime referenceTime, AgingRule agingRule) {
        if (waitingTokens == null || waitingTokens.isEmpty()) {
            return Collections.emptyList();
        }

        PriorityQueue<Token> pq = buildPriorityQueue(waitingTokens, referenceTime, agingRule);
        List<Token> ranked = new ArrayList<>(pq.size());

        while (!pq.isEmpty()) {
            ranked.add(pq.poll());
        }
        return ranked;
    }

    /**
     * Determines 1-based queue position for a target token among all waiting tokens.
     */
    public int getQueuePosition(Long tokenId, List<Token> waitingTokens, LocalDateTime referenceTime, AgingRule agingRule) {
        if (tokenId == null || waitingTokens == null || waitingTokens.isEmpty()) {
            return -1;
        }

        List<Token> ranked = getRankedWaitingTokens(waitingTokens, referenceTime, agingRule);
        for (int i = 0; i < ranked.size(); i++) {
            if (tokenId.equals(ranked.get(i).getTokenId())) {
                return i + 1; // 1-based position
            }
        }
        return -1;
    }

    /**
     * Calculates the number of customers ranked ahead of the target token.
     */
    public int getPeopleAhead(Long tokenId, List<Token> waitingTokens, LocalDateTime referenceTime, AgingRule agingRule) {
        int pos = getQueuePosition(tokenId, waitingTokens, referenceTime, agingRule);
        return pos > 0 ? pos - 1 : 0;
    }

    /**
     * Checks if token's service is supported by counter's capabilities.
     */
    private boolean isCompatible(Token token, Set<Long> supportedServiceIds) {
        if (token == null || token.getServiceId() == null) {
            return false;
        }
        if (supportedServiceIds == null || supportedServiceIds.isEmpty()) {
            return true; // If no restriction configured, counter accepts all services
        }
        return supportedServiceIds.contains(token.getServiceId());
    }
}
