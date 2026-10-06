package com.queuesense.controller;

import com.queuesense.model.Service;
import com.queuesense.service.ServiceManagementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ServiceServlet", urlPatterns = {"/services", "/services/create", "/services/update"})
public class ServiceServlet extends HttpServlet {

    private final ServiceManagementService serviceManagementService = new ServiceManagementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Service> services = serviceManagementService.getAllServices();
        req.setAttribute("services", services);
        req.getRequestDispatcher("/services.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        try {
            if ("/services/create".equals(path)) {
                Service s = new Service();
                s.setServiceCode(req.getParameter("serviceCode"));
                s.setServiceName(req.getParameter("serviceName"));
                s.setDescription(req.getParameter("description"));
                s.setAverageServiceMinutes(Integer.parseInt(req.getParameter("avgMinutes")));
                s.setPriorityWeight(Integer.parseInt(req.getParameter("priorityWeight")));
                s.setActive("true".equalsIgnoreCase(req.getParameter("active")));

                serviceManagementService.createService(s);
                resp.sendRedirect(req.getContextPath() + "/services?msg=created");
            } else if ("/services/update".equals(path)) {
                Long id = Long.parseLong(req.getParameter("serviceId"));
                Service s = serviceManagementService.getServiceById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Service ID not found: " + id));
                s.setServiceCode(req.getParameter("serviceCode"));
                s.setServiceName(req.getParameter("serviceName"));
                s.setDescription(req.getParameter("description"));
                s.setAverageServiceMinutes(Integer.parseInt(req.getParameter("avgMinutes")));
                s.setPriorityWeight(Integer.parseInt(req.getParameter("priorityWeight")));
                s.setActive("true".equalsIgnoreCase(req.getParameter("active")));

                serviceManagementService.updateService(s);
                resp.sendRedirect(req.getContextPath() + "/services?msg=updated");
            }
        } catch (Exception ex) {
            resp.sendRedirect(req.getContextPath() + "/services?error=" + java.net.URLEncoder.encode(ex.getMessage(), "UTF-8"));
        }
    }
}
