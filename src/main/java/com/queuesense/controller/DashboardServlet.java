package com.queuesense.controller;

import com.queuesense.dto.CounterWorkloadDto;
import com.queuesense.dto.DashboardStatsDto;
import com.queuesense.enums.UserRole;
import com.queuesense.model.User;
import com.queuesense.service.DashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard", ""})
public class DashboardServlet extends HttpServlet {

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Role-based homepage dispatch
        if (user.getRole() == UserRole.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/my-token");
            return;
        } else if (user.getRole() == UserRole.COUNTER_AGENT) {
            resp.sendRedirect(req.getContextPath() + "/agent/workspace");
            return;
        }

        // ADMIN / OPERATOR Dashboard
        String filter = req.getParameter("filter");
        if (filter == null || filter.trim().isEmpty()) {
            filter = "today";
        }

        LocalDate customStart = null;
        LocalDate customEnd = null;
        String startParam = req.getParameter("startDate");
        String endParam = req.getParameter("endDate");
        try {
            if (startParam != null && !startParam.isEmpty()) customStart = LocalDate.parse(startParam);
            if (endParam != null && !endParam.isEmpty()) customEnd = LocalDate.parse(endParam);
        } catch (Exception ignored) {}

        DashboardStatsDto stats = dashboardService.getStats(filter, customStart, customEnd);
        List<CounterWorkloadDto> workloads = dashboardService.getCounterWorkloads();

        req.setAttribute("stats", stats);
        req.setAttribute("workloads", workloads);
        req.setAttribute("currentFilter", filter);
        req.setAttribute("startDate", startParam != null ? startParam : "");
        req.setAttribute("endDate", endParam != null ? endParam : "");

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
