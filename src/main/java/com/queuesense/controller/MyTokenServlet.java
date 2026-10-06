package com.queuesense.controller;

import com.queuesense.dto.QueueItemDto;
import com.queuesense.enums.UserRole;
import com.queuesense.model.Customer;
import com.queuesense.model.Token;
import com.queuesense.model.User;
import com.queuesense.service.CustomerService;
import com.queuesense.service.QueueService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "MyTokenServlet", urlPatterns = {"/my-token"})
public class MyTokenServlet extends HttpServlet {

    private final QueueService queueService = new QueueService();
    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Customer customer = customerService.findByUserId(user.getUserId()).orElse(null);

        // If user is not yet linked to a customer record, create or find one for demo/convenience
        if (customer == null && user.getRole() == UserRole.CUSTOMER) {
            Customer newCust = new Customer();
            newCust.setUserId(user.getUserId());
            newCust.setFullName(user.getFullName());
            newCust.setEmail(user.getEmail());
            newCust.setPhone("+1-555-0100");
            customer = customerService.registerCustomer(newCust);
        }

        if (customer != null) {
            List<Token> activeTokens = queueService.getActiveTokensForCustomer(customer.getCustomerId());
            List<Token> historyTokens = queueService.getCustomerTokenHistory(customer.getCustomerId());

            // If customer has an active waiting token, look up their dynamic queue position and people ahead
            List<QueueItemDto> liveQueue = queueService.getLiveQueueDisplayItems();
            QueueItemDto activeQueueInfo = null;

            if (!activeTokens.isEmpty()) {
                Token primary = activeTokens.get(0);
                for (QueueItemDto item : liveQueue) {
                    if (item.getTokenId().equals(primary.getTokenId())) {
                        activeQueueInfo = item;
                        break;
                    }
                }
            }

            req.setAttribute("customer", customer);
            req.setAttribute("activeTokens", activeTokens);
            req.setAttribute("activeQueueInfo", activeQueueInfo);
            req.setAttribute("historyTokens", historyTokens);
        }

        req.getRequestDispatcher("/my-token.jsp").forward(req, resp);
    }
}
