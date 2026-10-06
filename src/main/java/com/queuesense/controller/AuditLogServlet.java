package com.queuesense.controller;

import com.queuesense.dao.AuditLogDao;
import com.queuesense.model.AuditLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AuditLogServlet", urlPatterns = {"/audit-logs"})
public class AuditLogServlet extends HttpServlet {

    private final AuditLogDao auditLogDao = new AuditLogDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = 1;
        int pageSize = 25;
        try {
            String p = req.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
        } catch (NumberFormatException ignored) {}

        int offset = (page - 1) * pageSize;
        List<AuditLog> logs = auditLogDao.findAll(pageSize, offset);
        int total = auditLogDao.countAll();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("logs", logs);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", Math.max(1, totalPages));
        req.setAttribute("totalCount", total);

        req.getRequestDispatcher("/audit-logs.jsp").forward(req, resp);
    }
}
