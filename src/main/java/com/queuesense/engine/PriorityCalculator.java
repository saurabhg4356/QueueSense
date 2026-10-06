package com.queuesense.engine;

import com.queuesense.model.AgingRule;
import com.queuesense.model.Token;

import java.time.LocalDateTime;

/**
 * Deterministic Priority Calculator with Anti-Starvation Aging.
 *
 * Implements the formula:
 * score = basePriorityWeight
 *       + (servicePriorityWeight * 0.2)
 *       + (waitingMinutes * 0.5)
 *       + specialAssistanceBonus
 *       + agingBonus
 *       + starvationFairnessBoost (if waitingMinutes >= starvationThreshold)
 */
public class PriorityCalculator {

    private PriorityCalculator() {}

    /**
     * Calculates the dynamic priority score of a token at a given reference time.
     */
    public static double calculateScore(Token token, LocalDateTime referenceTime, AgingRule agingRule) {
        if (token == null) {
            return 0.0;
        }

        AgingRule rule = agingRule != null ? agingRule : new AgingRule();

        // 1. Base Priority Weight
        double baseWeight = token.getPriorityType() != null ? token.getPriorityType().getDefaultWeight() : 10.0;

        // 2. Service Weight factor (scaled 20%)
        double serviceWeightBonus = token.getServicePriorityWeight() * 0.2;

        // 3. Waiting Time calculation
        long waitingMinutes = token.getWaitingMinutesNow(referenceTime);
        double waitingTimeBonus = waitingMinutes * 0.5;

        // 4. Special Assistance Bonus
        double assistanceBonus = 0.0;
        if (token.isSpecialAssistanceRequired()) {
            assistanceBonus = rule.getSpecialAssistanceBonus();
        }

        // 5. Anti-Starvation Aging Bonus
        double agingBonus = calculateAgingBonus(waitingMinutes, rule);

        // 6. Starvation Fairness Boost
        // If a customer has been waiting past the configured starvation threshold,
        // an accelerated boost is added to ensure priority inversion does not starve normal customers indefinitely.
        double starvationBoost = 0.0;
        if (waitingMinutes >= rule.getStarvationThresholdMinutes()) {
            long minutesBeyond = waitingMinutes - rule.getStarvationThresholdMinutes();
            starvationBoost = 10.0 + (minutesBeyond * 0.5);
        }

        return Math.round((baseWeight + serviceWeightBonus + waitingTimeBonus + assistanceBonus + agingBonus + starvationBoost) * 100.0) / 100.0;
    }

    /**
     * Calculates the incremental aging bonus based on elapsed intervals and maximum cap.
     */
    public static double calculateAgingBonus(long waitingMinutes, AgingRule rule) {
        if (rule == null || !rule.isActive() || waitingMinutes <= 0 || rule.getMinutesInterval() <= 0) {
            return 0.0;
        }

        long intervals = waitingMinutes / rule.getMinutesInterval();
        double calculatedBonus = intervals * (double) rule.getBonusPoints();

        return Math.min(rule.getMaximumBonus(), calculatedBonus);
    }
}
