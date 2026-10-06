package com.queuesense.controller;

import com.queuesense.dao.FeedbackDao;
import com.queuesense.dao.TokenDao;
import com.queuesense.model.Customer;
import com.queuesense.model.Feedback;
import com.queuesense.model.Token;
import com.queuesense.model.User;
import com.queuesense.service.CustomerService;
import com.queuesense.service.QueueService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "FeedbackServlet", urlPatterns = {"/feedback", "/feedback/submit"})
public class FeedbackServlet extends HttpServlet {

    private final QueueService queueService = new QueueService();
    private final CustomerService customerService = new CustomerService();
    private final TokenDao tokenDao = new TokenDao();
    private final FeedbackDao feedbackDao = new FeedbackDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        String tokenIdParam = req.getParameter("tokenId");
        if (tokenIdParam != null && !tokenIdParam.isEmpty()) {
            Long tokenId = Long.parseLong(tokenIdParam);
            Token token = tokenDao.findById(tokenId).orElse(null);
            req.setAttribute("token", token);
        }

        // If admin/operator, load all feedback
        List<Feedback> allFeedback = feedbackDao.findAll(50, 0);
        req.setAttribute("allFeedback", allFeedback);
        req.setAttribute("avgRating", Math.round(feedbackDao.getAverageRating() * 10.0) / 10.0);

        req.getRequestDispatcher("/feedback.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        try {
            Long tokenId = Long.parseLong(req.getParameter("tokenId"));
            int rating = Integer.parseInt(req.getParameter("rating"));
            String comment = req.getParameter("comment");

            Customer customer = customerService.findByUserId(user.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer record not associated with this account."));

            queueService.submitFeedback(tokenId, customer.getCustomerId(), rating, comment);
            resp.sendRedirect(req.getContextPath() + "/my-token?msg=" +
                    java.net.URLEncoder.encode("Thank you for your valuable feedback!", "UTF-8"));

        } catch (Exception ex) {
            resp.sendRedirect(req.getContextPath() + "/feedback?tokenId=" + req.getParameter("tokenId") +
                    "&error=" + java.net.URLEncoder.encode(ex.getMessage(), "UTF-8"));
        }
    }
}
