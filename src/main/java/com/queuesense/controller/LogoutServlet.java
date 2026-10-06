package com.queuesense.controller;

import com.queuesense.model.User;
import com.queuesense.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("currentUser");
            authService.logLogout(user, req.getRemoteAddr());
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login?msg=logged_out");
    }
}
