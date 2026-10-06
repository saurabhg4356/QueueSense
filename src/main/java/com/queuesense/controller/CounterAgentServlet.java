package com.queuesense.controller;

import com.queuesense.dao.CounterDao;
import com.queuesense.dao.ServiceSessionDao;
import com.queuesense.dao.TokenDao;
import com.queuesense.dto.QueueItemDto;
import com.queuesense.enums.PauseReason;
import com.queuesense.enums.TransferReason;
import com.queuesense.model.Counter;
import com.queuesense.model.ServicePause;
import com.queuesense.model.ServiceSession;
import com.queuesense.model.Token;
import com.queuesense.model.User;
import com.queuesense.service.CounterService;
import com.queuesense.service.QueueService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "CounterAgentServlet", urlPatterns = {"/agent/workspace", "/agent/action"})
public class CounterAgentServlet extends HttpServlet {

    private final QueueService queueService = new QueueService();
    private final CounterService counterService = new CounterService();
    private final CounterDao counterDao = new CounterDao();
    private final TokenDao tokenDao = new TokenDao();
    private final ServiceSessionDao sessionDao = new ServiceSessionDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Support counter selection by admin/operator or agent's assigned counter
        Counter counter = null;
        String counterIdParam = req.getParameter("counterId");
        if (counterIdParam != null && !counterIdParam.isEmpty()) {
            counter = counterDao.findById(Long.parseLong(counterIdParam)).orElse(null);
        } else {
            counter = counterDao.findByAssignedAgentId(user.getUserId()).orElse(null);
        }

        if (counter == null) {
            List<Counter> allCounters = counterDao.findAll();
            req.setAttribute("allCounters", allCounters);
            req.setAttribute("errorMessage", "No counter currently assigned to your user account. Please select a counter to operate.");
            req.getRequestDispatcher("/service-session.jsp").forward(req, resp);
            return;
        }

        Optional<Token> activeToken = tokenDao.findCurrentServingTokenForCounter(counter.getCounterId());
        ServiceSession activeSession = null;
        List<ServicePause> pauses = null;

        if (activeToken.isPresent()) {
            activeSession = sessionDao.findActiveSessionByTokenId(activeToken.get().getTokenId()).orElse(null);
            if (activeSession != null) {
                pauses = sessionDao.findPausesBySessionId(activeSession.getSessionId());
            }
        }

        // Retrieve waiting tokens compatible with this counter
        List<QueueItemDto> allWaiting = queueService.getLiveQueueDisplayItems();
        final Counter currentCounter = counter;
        List<QueueItemDto> compatibleWaiting = allWaiting.stream()
                .filter(item -> currentCounter.supportsService(item.getServiceId()))
                .toList();

        List<Counter> allCounters = counterDao.findAll();

        req.setAttribute("counter", counter);
        req.setAttribute("activeToken", activeToken.orElse(null));
        req.setAttribute("activeSession", activeSession);
        req.setAttribute("pauses", pauses);
        req.setAttribute("compatibleWaiting", compatibleWaiting);
        req.setAttribute("allCounters", allCounters);
        req.setAttribute("pauseReasons", PauseReason.values());
        req.setAttribute("transferReasons", TransferReason.values());

        req.getRequestDispatcher("/service-session.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;
        Long userId = (user != null) ? user.getUserId() : null;

        String action = req.getParameter("action");
        String counterIdParam = req.getParameter("counterId");
        Long counterId = (counterIdParam != null && !counterIdParam.isEmpty()) ? Long.parseLong(counterIdParam) : null;

        try {
            if ("callNext".equals(action)) {
                Token t = queueService.callNextToken(counterId, userId);
                resp.sendRedirect(req.getContextPath() + "/agent/workspace?counterId=" + counterId +
                        "&msg=" + java.net.URLEncoder.encode("Called Token " + t.getTokenNumber(), "UTF-8"));
                return;
            }

            Long tokenId = Long.parseLong(req.getParameter("tokenId"));

            switch (action) {
                case "start" -> queueService.startService(tokenId, counterId, userId);
                case "pause" -> {
                    PauseReason reason = PauseReason.valueOf(req.getParameter("pauseReason"));
                    String remarks = req.getParameter("remarks");
                    queueService.pauseService(tokenId, reason, remarks, userId);
                }
                case "resume" -> queueService.resumeService(tokenId, userId);
                case "complete" -> {
                    String remarks = req.getParameter("remarks");
                    queueService.completeService(tokenId, remarks, userId);
                }
                case "noShow" -> queueService.markNoShow(tokenId, userId);
                case "transfer" -> {
                    Long targetCounterId = Long.parseLong(req.getParameter("targetCounterId"));
                    TransferReason reason = TransferReason.valueOf(req.getParameter("transferReason"));
                    String remarks = req.getParameter("remarks");
                    queueService.transferToken(tokenId, counterId, targetCounterId, reason, remarks, userId);
                }
                default -> throw new IllegalArgumentException("Unknown agent action: " + action);
            }

            resp.sendRedirect(req.getContextPath() + "/agent/workspace?counterId=" + counterId +
                    "&msg=" + java.net.URLEncoder.encode("Action '" + action + "' performed successfully", "UTF-8"));

        } catch (Exception ex) {
            resp.sendRedirect(req.getContextPath() + "/agent/workspace?counterId=" + (counterId != null ? counterId : "") +
                    "&error=" + java.net.URLEncoder.encode(ex.getMessage(), "UTF-8"));
        }
    }
}
