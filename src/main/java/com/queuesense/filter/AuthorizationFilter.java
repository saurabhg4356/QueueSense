package com.queuesense.filter;

import com.queuesense.enums.UserRole;
import com.queuesense.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (user == null) {
            // Handled by AuthenticationFilter
            chain.doFilter(request, response);
            return;
        }

        UserRole role = user.getRole();

        // Admin has universal access
        if (role == UserRole.ADMIN) {
            chain.doFilter(request, response);
            return;
        }

        // Restrict Admin-only management endpoints
        if (path.startsWith("/priority-rules") ||
            path.startsWith("/aging-rules") ||
            path.startsWith("/audit-logs") ||
            path.startsWith("/users")) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized.jsp");
            return;
        }

        // Customer Role Permissions
        if (role == UserRole.CUSTOMER) {
            boolean allowed = path.equals("/") ||
                              path.equals("/dashboard") ||
                              path.equals("/my-token") ||
                              path.equals("/feedback") ||
                              path.equals("/profile") ||
                              path.startsWith("/api/customer/");
            if (!allowed && !path.startsWith("/css/") && !path.startsWith("/js/")) {
                resp.sendRedirect(req.getContextPath() + "/unauthorized.jsp");
                return;
            }
        }

        // Counter Agent Role Permissions
        if (role == UserRole.COUNTER_AGENT) {
            if (path.startsWith("/services/manage") || path.startsWith("/counters/manage")) {
                resp.sendRedirect(req.getContextPath() + "/unauthorized.jsp");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
