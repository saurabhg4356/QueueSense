package com.queuesense.controller;

import com.queuesense.dto.ReportItemDto;
import com.queuesense.service.DashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "ReportServlet", urlPatterns = {"/reports"})
public class ReportServlet extends HttpServlet {

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String filter = req.getParameter("filter");
        if (filter == null || filter.trim().isEmpty()) {
            filter = "today";
        }

        String reportType = req.getParameter("type");
        if (reportType == null || reportType.trim().isEmpty()) {
            reportType = "daily";
        }

        LocalDate customStart = null;
        LocalDate customEnd = null;
        String startParam = req.getParameter("startDate");
        String endParam = req.getParameter("endDate");
        try {
            if (startParam != null && !startParam.isEmpty()) customStart = LocalDate.parse(startParam);
            if (endParam != null && !endParam.isEmpty()) customEnd = LocalDate.parse(endParam);
        } catch (Exception ignored) {}

        List<ReportItemDto> reportData;
        switch (reportType) {
            case "demand" -> reportData = dashboardService.getDemandReport(filter, customStart, customEnd);
            case "utilization" -> reportData = dashboardService.getUtilizationReport(filter, customStart, customEnd);
            default -> reportData = dashboardService.getDailyReport(filter, customStart, customEnd);
        }

        req.setAttribute("reportData", reportData);
        req.setAttribute("reportType", reportType);
        req.setAttribute("currentFilter", filter);
        req.setAttribute("startDate", startParam != null ? startParam : "");
        req.setAttribute("endDate", endParam != null ? endParam : "");

        req.getRequestDispatcher("/reports.jsp").forward(req, resp);
    }
}
