package com.queuesense.controller;

import com.queuesense.dto.CounterWorkloadDto;
import com.queuesense.dto.QueueItemDto;
import com.queuesense.model.Counter;
import com.queuesense.model.Token;
import com.queuesense.model.User;
import com.queuesense.service.CounterService;
import com.queuesense.service.DashboardService;
import com.queuesense.service.QueueService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "QueueServlet", urlPatterns = {"/queue", "/queue/call-next", "/queue/cancel"})
public class QueueServlet extends HttpServlet {

    private final QueueService queueService = new QueueService();
    private final CounterService counterService = new CounterService();
    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<QueueItemDto> queueItems = queueService.getLiveQueueDisplayItems();
        List<Counter> counters = counterService.getAllCounters();
        List<CounterWorkloadDto> workloads = dashboardService.getCounterWorkloads();

        req.setAttribute("queueItems", queueItems);
        req.setAttribute("counters", counters);
        req.setAttribute("workloads", workloads);

        req.getRequestDispatcher("/queue.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;
        Long userId = (user != null) ? user.getUserId() : null;

        try {
            if ("/queue/call-next".equals(path)) {
                Long counterId = Long.parseLong(req.getParameter("counterId"));
                Token calledToken = queueService.callNextToken(counterId, userId);
                resp.sendRedirect(req.getContextPath() + "/queue?msg=" +
                        java.net.URLEncoder.encode("Counter called Token " + calledToken.getTokenNumber(), "UTF-8"));
            } else if ("/queue/cancel".equals(path)) {
                Long tokenId = Long.parseLong(req.getParameter("tokenId"));
                String reason = req.getParameter("reason");
                queueService.cancelToken(tokenId, userId, reason != null ? reason : "Cancelled by operator");
                resp.sendRedirect(req.getContextPath() + "/queue?msg=" +
                        java.net.URLEncoder.encode("Token cancelled successfully", "UTF-8"));
            }
        } catch (Exception ex) {
            resp.sendRedirect(req.getContextPath() + "/queue?error=" +
                    java.net.URLEncoder.encode(ex.getMessage(), "UTF-8"));
        }
    }
}
