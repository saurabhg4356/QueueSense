package com.queuesense.service;

import com.queuesense.dao.RuleDao;
import com.queuesense.model.AgingRule;
import com.queuesense.model.PriorityRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RuleService {

    private static final Logger logger = LoggerFactory.getLogger(RuleService.class);
    private final RuleDao ruleDao;

    public RuleService() {
        this.ruleDao = new RuleDao();
    }

    public RuleService(RuleDao ruleDao) {
        this.ruleDao = ruleDao;
    }

    public List<PriorityRule> getAllPriorityRules() {
        return ruleDao.findAllPriorityRules();
    }

    public AgingRule getActiveAgingRule() {
        return ruleDao.getActiveAgingRule();
    }

    public void updatePriorityRule(PriorityRule rule) {
        ruleDao.updatePriorityRule(rule);
        logger.info("Updated priority rule for {}", rule.getPriorityType());
    }

    public void updateAgingRule(AgingRule rule) {
        ruleDao.updateAgingRule(rule);
        logger.info("Updated aging rule ID {}", rule.getId());
    }
}
