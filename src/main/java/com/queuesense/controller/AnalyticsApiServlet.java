package com.queuesense.controller;

import com.google.gson.Gson;
import com.queuesense.dto.ChartDataDto;
import com.queuesense.service.DashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@WebServlet(name = "AnalyticsApiServlet", urlPatterns = {"/api/analytics/charts"})
public class AnalyticsApiServlet extends HttpServlet {

    private final DashboardService dashboardService = new DashboardService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        Map<String, ChartDataDto> charts = dashboardService.getAllChartData(filter, customStart, customEnd);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(charts));
    }
}
