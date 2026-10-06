package com.queuesense.controller;

import com.queuesense.dao.FeedbackDao;
import com.queuesense.dao.ServiceSessionDao;
import com.queuesense.dao.TokenDao;
import com.queuesense.dao.TokenStatusHistoryDao;
import com.queuesense.dao.TokenTransferDao;
import com.queuesense.enums.PriorityType;
import com.queuesense.enums.TokenStatus;
import com.queuesense.model.*;
import com.queuesense.service.CounterService;
import com.queuesense.service.ServiceManagementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "TokenServlet", urlPatterns = {"/tokens", "/tokens/details"})
public class TokenServlet extends HttpServlet {

    private final TokenDao tokenDao = new TokenDao();
    private final TokenStatusHistoryDao statusHistoryDao = new TokenStatusHistoryDao();
    private final ServiceSessionDao sessionDao = new ServiceSessionDao();
    private final TokenTransferDao transferDao = new TokenTransferDao();
    private final FeedbackDao feedbackDao = new FeedbackDao();
    private final ServiceManagementService serviceManagementService = new ServiceManagementService();
    private final CounterService counterService = new CounterService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/tokens/details".equals(path)) {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/tokens");
                return;
            }
            try {
                Long tokenId = Long.parseLong(idParam);
                Token token = tokenDao.findById(tokenId)
                        .orElseThrow(() -> new IllegalArgumentException("Token ID not found: " + tokenId));

                List<TokenStatusHistory> histories = statusHistoryDao.findByTokenId(tokenId);
                List<TokenTransfer> transfers = transferDao.findByTokenId(tokenId);
                Optional<Feedback> feedback = feedbackDao.findByTokenId(tokenId);
                Optional<ServiceSession> activeSession = sessionDao.findActiveSessionByTokenId(tokenId);

                req.setAttribute("token", token);
                req.setAttribute("histories", histories);
                req.setAttribute("transfers", transfers);
                req.setAttribute("feedback", feedback.orElse(null));
                req.setAttribute("activeSession", activeSession.orElse(null));

                req.getRequestDispatcher("/token-details.jsp").forward(req, resp);
                return;
            } catch (Exception ex) {
                resp.sendRedirect(req.getContextPath() + "/tokens?error=" + ex.getMessage());
                return;
            }
        }

        // Search & Filter
        String query = req.getParameter("q");
        String statusParam = req.getParameter("status");
        String priorityParam = req.getParameter("priority");
        String serviceParam = req.getParameter("serviceId");
        String counterParam = req.getParameter("counterId");
        String startParam = req.getParameter("startDate");
        String endParam = req.getParameter("endDate");

        TokenStatus status = (statusParam != null && !statusParam.isEmpty()) ? TokenStatus.valueOf(statusParam) : null;
        PriorityType priority = (priorityParam != null && !priorityParam.isEmpty()) ? PriorityType.valueOf(priorityParam) : null;
        Long serviceId = (serviceParam != null && !serviceParam.isEmpty()) ? Long.parseLong(serviceParam) : null;
        Long counterId = (counterParam != null && !counterParam.isEmpty()) ? Long.parseLong(counterParam) : null;
        LocalDate startDate = (startParam != null && !startParam.isEmpty()) ? LocalDate.parse(startParam) : null;
        LocalDate endDate = (endParam != null && !endParam.isEmpty()) ? LocalDate.parse(endParam) : null;

        int page = 1;
        int pageSize = 15;
        try {
            String pageParam = req.getParameter("page");
            if (pageParam != null) page = Math.max(1, Integer.parseInt(pageParam));
        } catch (NumberFormatException ignored) {}

        int offset = (page - 1) * pageSize;
        List<Token> tokens = tokenDao.searchTokens(query, status, priority, serviceId, counterId, startDate, endDate, pageSize, offset);
        int totalCount = tokenDao.countSearchTokens(query, status, priority, serviceId, counterId, startDate, endDate);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        req.setAttribute("tokens", tokens);
        req.setAttribute("query", query != null ? query : "");
        req.setAttribute("status", statusParam != null ? statusParam : "");
        req.setAttribute("priority", priorityParam != null ? priorityParam : "");
        req.setAttribute("serviceId", serviceParam != null ? serviceParam : "");
        req.setAttribute("counterId", counterParam != null ? counterParam : "");
        req.setAttribute("startDate", startParam != null ? startParam : "");
        req.setAttribute("endDate", endParam != null ? endParam : "");

        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", Math.max(1, totalPages));
        req.setAttribute("totalCount", totalCount);

        req.setAttribute("services", serviceManagementService.getAllServices());
        req.setAttribute("counters", counterService.getAllCounters());

        req.getRequestDispatcher("/tokens.jsp").forward(req, resp);
    }
}
