package com.queuesense.controller;

import com.queuesense.enums.PriorityType;
import com.queuesense.model.Customer;
import com.queuesense.model.Service;
import com.queuesense.model.Token;
import com.queuesense.model.User;
import com.queuesense.service.CustomerService;
import com.queuesense.service.QueueService;
import com.queuesense.service.ServiceManagementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "CreateTokenServlet", urlPatterns = {"/tokens/create"})
public class CreateTokenServlet extends HttpServlet {

    private final QueueService queueService = new QueueService();
    private final CustomerService customerService = new CustomerService();
    private final ServiceManagementService serviceManagementService = new ServiceManagementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Service> services = serviceManagementService.getActiveServices();
        req.setAttribute("services", services);
        req.setAttribute("priorityTypes", PriorityType.values());

        String customerIdParam = req.getParameter("customerId");
        if (customerIdParam != null && !customerIdParam.isEmpty()) {
            try {
                Customer c = customerService.getCustomerById(Long.parseLong(customerIdParam));
                req.setAttribute("selectedCustomer", c);
            } catch (Exception ignored) {}
        }

        req.getRequestDispatcher("/create-token.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;
        Long creatorId = (user != null) ? user.getUserId() : null;

        try {
            Long customerId;
            String customerIdParam = req.getParameter("customerId");

            // Option 1: Existing customer selected
            if (customerIdParam != null && !customerIdParam.trim().isEmpty()) {
                customerId = Long.parseLong(customerIdParam.trim());
            } else {
                // Option 2: Quick-register customer on the fly
                Customer newCust = new Customer();
                newCust.setFullName(req.getParameter("customerName"));
                newCust.setPhone(req.getParameter("customerPhone"));
                newCust.setEmail(req.getParameter("customerEmail"));
                newCust.setSpecialAssistanceRequired("true".equalsIgnoreCase(req.getParameter("specialAssistance")));
                Customer registered = customerService.registerCustomer(newCust);
                customerId = registered.getCustomerId();
            }

            Long serviceId = Long.parseLong(req.getParameter("serviceId"));
            PriorityType priorityType = PriorityType.valueOf(req.getParameter("priorityType"));

            Token token = queueService.createToken(customerId, serviceId, priorityType, creatorId);
            resp.sendRedirect(req.getContextPath() + "/tokens/details?id=" + token.getTokenId() + "&msg=created");

        } catch (Exception ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            doGet(req, resp);
        }
    }
}
