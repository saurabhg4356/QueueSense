package com.queuesense.controller;

import com.queuesense.exception.UnauthorizedAccessException;
import com.queuesense.model.User;
import com.queuesense.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String redirect = req.getParameter("redirect");
        String ipAddress = req.getRemoteAddr();

        try {
            User user = authService.authenticate(username, password, ipAddress);
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", user);
            session.setAttribute("userRole", user.getRole().name());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFullName());

            if (redirect != null && !redirect.trim().isEmpty() && !redirect.contains("login")) {
                resp.sendRedirect(req.getContextPath() + redirect);
            } else {
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            }
        } catch (UnauthorizedAccessException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            req.setAttribute("prevUsername", username);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
