package com.queuesense.controller;

import com.queuesense.enums.CounterStatus;
import com.queuesense.model.Counter;
import com.queuesense.model.Service;
import com.queuesense.service.CounterService;
import com.queuesense.service.ServiceManagementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "CounterServlet", urlPatterns = {"/counters", "/counters/details", "/counters/create", "/counters/update", "/counters/compatibility"})
public class CounterServlet extends HttpServlet {

    private final CounterService counterService = new CounterService();
    private final ServiceManagementService serviceManagementService = new ServiceManagementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/counters/details".equals(path)) {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/counters");
                return;
            }
            try {
                Long counterId = Long.parseLong(idParam);
                Counter counter = counterService.getCounterById(counterId);
                List<Service> allServices = serviceManagementService.getAllServices();
                req.setAttribute("counter", counter);
                req.setAttribute("allServices", allServices);
                req.getRequestDispatcher("/counter-details.jsp").forward(req, resp);
                return;
            } catch (Exception ex) {
                resp.sendRedirect(req.getContextPath() + "/counters?error=" + ex.getMessage());
                return;
            }
        }

        List<Counter> counters = counterService.getAllCounters();
        List<Service> allServices = serviceManagementService.getAllServices();
        req.setAttribute("counters", counters);
        req.setAttribute("allServices", allServices);
        req.getRequestDispatcher("/counters.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        try {
            if ("/counters/create".equals(path)) {
                Counter c = new Counter();
                c.setCounterNumber(Integer.parseInt(req.getParameter("counterNumber")));
                c.setCounterName(req.getParameter("counterName"));
                c.setLocation(req.getParameter("location"));
                c.setStatus(CounterStatus.valueOf(req.getParameter("status")));

                String[] serviceIdsArr = req.getParameterValues("serviceIds");
                Set<Long> serviceIds = new HashSet<>();
                if (serviceIdsArr != null) {
                    for (String sId : serviceIdsArr) {
                        serviceIds.add(Long.parseLong(sId));
                    }
                }

                counterService.createCounter(c, serviceIds);
                resp.sendRedirect(req.getContextPath() + "/counters?msg=created");
            } else if ("/counters/update".equals(path)) {
                Long id = Long.parseLong(req.getParameter("counterId"));
                Counter c = counterService.getCounterById(id);
                c.setCounterNumber(Integer.parseInt(req.getParameter("counterNumber")));
                c.setCounterName(req.getParameter("counterName"));
                c.setLocation(req.getParameter("location"));
                c.setStatus(CounterStatus.valueOf(req.getParameter("status")));

                String[] serviceIdsArr = req.getParameterValues("serviceIds");
                Set<Long> serviceIds = new HashSet<>();
                if (serviceIdsArr != null) {
                    for (String sId : serviceIdsArr) {
                        serviceIds.add(Long.parseLong(sId));
                    }
                }

                counterService.updateCounter(c, serviceIds);
                resp.sendRedirect(req.getContextPath() + "/counters/details?id=" + id + "&msg=updated");
            }
        } catch (Exception ex) {
            resp.sendRedirect(req.getContextPath() + "/counters?error=" + java.net.URLEncoder.encode(ex.getMessage(), "UTF-8"));
        }
    }
}
