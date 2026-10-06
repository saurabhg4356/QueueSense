package com.queuesense.controller;

import com.queuesense.enums.PriorityType;
import com.queuesense.model.AgingRule;
import com.queuesense.model.PriorityRule;
import com.queuesense.service.RuleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "RuleServlet", urlPatterns = {"/priority-rules", "/priority-rules/update", "/aging-rules", "/aging-rules/update"})
public class RuleServlet extends HttpServlet {

    private final RuleService ruleService = new RuleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if (path.startsWith("/priority-rules")) {
            List<PriorityRule> rules = ruleService.getAllPriorityRules();
            req.setAttribute("rules", rules);
            req.getRequestDispatcher("/priority-rules.jsp").forward(req, resp);
        } else {
            AgingRule agingRule = ruleService.getActiveAgingRule();
            req.setAttribute("agingRule", agingRule);
            req.getRequestDispatcher("/aging-rules.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        try {
            if ("/priority-rules/update".equals(path)) {
                Long id = Long.parseLong(req.getParameter("ruleId"));
                int baseWeight = Integer.parseInt(req.getParameter("baseWeight"));
                String description = req.getParameter("description");
                PriorityType type = PriorityType.valueOf(req.getParameter("priorityType"));

                PriorityRule rule = new PriorityRule(id, type, baseWeight, description, true);
                ruleService.updatePriorityRule(rule);
                resp.sendRedirect(req.getContextPath() + "/priority-rules?msg=updated");
            } else if ("/aging-rules/update".equals(path)) {
                Long id = Long.parseLong(req.getParameter("agingRuleId"));
                int minutesInterval = Integer.parseInt(req.getParameter("minutesInterval"));
                int bonusPoints = Integer.parseInt(req.getParameter("bonusPoints"));
                int maximumBonus = Integer.parseInt(req.getParameter("maximumBonus"));
                int starvationThreshold = Integer.parseInt(req.getParameter("starvationThreshold"));
                int specialAssistanceBonus = Integer.parseInt(req.getParameter("specialAssistanceBonus"));

                AgingRule rule = new AgingRule(id, minutesInterval, bonusPoints, maximumBonus, starvationThreshold, specialAssistanceBonus, true);
                ruleService.updateAgingRule(rule);
                resp.sendRedirect(req.getContextPath() + "/aging-rules?msg=updated");
            }
        } catch (Exception ex) {
            String redirectUrl = path.startsWith("/priority-rules") ? "/priority-rules" : "/aging-rules";
            resp.sendRedirect(req.getContextPath() + redirectUrl + "?error=" + java.net.URLEncoder.encode(ex.getMessage(), "UTF-8"));
        }
    }
}
