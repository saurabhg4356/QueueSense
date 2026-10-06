package com.queuesense.controller;

import com.queuesense.model.Customer;
import com.queuesense.model.Token;
import com.queuesense.service.CustomerService;
import com.queuesense.service.QueueService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "CustomerServlet", urlPatterns = {"/customers", "/customers/details", "/customers/create", "/customers/update"})
public class CustomerServlet extends HttpServlet {

    private final CustomerService customerService = new CustomerService();
    private final QueueService queueService = new QueueService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/customers/details".equals(path)) {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/customers");
                return;
            }
            try {
                Long customerId = Long.parseLong(idParam);
                Customer customer = customerService.getCustomerById(customerId);
                List<Token> tokenHistory = queueService.getCustomerTokenHistory(customerId);
                req.setAttribute("customer", customer);
                req.setAttribute("tokenHistory", tokenHistory);
                req.getRequestDispatcher("/customer-details.jsp").forward(req, resp);
                return;
            } catch (Exception ex) {
                resp.sendRedirect(req.getContextPath() + "/customers?error=" + ex.getMessage());
                return;
            }
        }

        // List / Search
        String query = req.getParameter("q");
        int page = 1;
        int pageSize = 10;
        try {
            String pageParam = req.getParameter("page");
            if (pageParam != null) page = Math.max(1, Integer.parseInt(pageParam));
        } catch (NumberFormatException ignored) {}

        List<Customer> customers = customerService.searchCustomers(query, page, pageSize);
        int totalCount = customerService.countCustomers(query);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        req.setAttribute("customers", customers);
        req.setAttribute("query", query != null ? query : "");
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", Math.max(1, totalPages));
        req.setAttribute("totalCount", totalCount);

        req.getRequestDispatcher("/customers.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        try {
            if ("/customers/create".equals(path)) {
                Customer c = new Customer();
                c.setFullName(req.getParameter("fullName"));
                c.setEmail(req.getParameter("email"));
                c.setPhone(req.getParameter("phone"));
                String dob = req.getParameter("dateOfBirth");
                if (dob != null && !dob.isEmpty()) c.setDateOfBirth(LocalDate.parse(dob));
                c.setSpecialAssistanceRequired("true".equalsIgnoreCase(req.getParameter("specialAssistance")));

                Customer created = customerService.registerCustomer(c);
                resp.sendRedirect(req.getContextPath() + "/customers/details?id=" + created.getCustomerId() + "&msg=created");
            } else if ("/customers/update".equals(path)) {
                Long id = Long.parseLong(req.getParameter("customerId"));
                Customer c = customerService.getCustomerById(id);
                c.setFullName(req.getParameter("fullName"));
                c.setEmail(req.getParameter("email"));
                c.setPhone(req.getParameter("phone"));
                String dob = req.getParameter("dateOfBirth");
                if (dob != null && !dob.isEmpty()) c.setDateOfBirth(LocalDate.parse(dob));
                c.setSpecialAssistanceRequired("true".equalsIgnoreCase(req.getParameter("specialAssistance")));

                customerService.updateCustomer(c);
                resp.sendRedirect(req.getContextPath() + "/customers/details?id=" + id + "&msg=updated");
            }
        } catch (Exception ex) {
            resp.sendRedirect(req.getContextPath() + "/customers?error=" + java.net.URLEncoder.encode(ex.getMessage(), "UTF-8"));
        }
    }
}
