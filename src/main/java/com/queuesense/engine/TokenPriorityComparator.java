package com.queuesense.engine;

import com.queuesense.model.AgingRule;
import com.queuesense.model.Token;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Custom Comparator for PriorityQueue ordering.
 * Configured as a Max-Priority ordering:
 * 1. Higher dynamic priority score served first.
 * 2. Earlier issued_at timestamp serves as FIFO tie-breaker for identical scores.
 * 3. Special assistance flag prioritizes vulnerable customers.
 * 4. Token ID serves as deterministic final tie-breaker.
 */
public class TokenPriorityComparator implements Comparator<Token> {

    private final LocalDateTime referenceTime;
    private final AgingRule agingRule;

    public TokenPriorityComparator(LocalDateTime referenceTime, AgingRule agingRule) {
        this.referenceTime = referenceTime != null ? referenceTime : LocalDateTime.now();
        this.agingRule = agingRule;
    }

    @Override
    public int compare(Token t1, Token t2) {
        if (t1 == t2) return 0;
        if (t1 == null) return 1;
        if (t2 == null) return -1;

        double score1 = PriorityCalculator.calculateScore(t1, referenceTime, agingRule);
        double score2 = PriorityCalculator.calculateScore(t2, referenceTime, agingRule);

        // Max-priority: token with HIGHER score comes first (returns negative)
        int scoreCompare = Double.compare(score2, score1);
        if (scoreCompare != 0) {
            return scoreCompare;
        }

        // Secondary: FIFO tie-breaker (earlier arrival comes first)
        if (t1.getIssuedAt() != null && t2.getIssuedAt() != null) {
            int timeCompare = t1.getIssuedAt().compareTo(t2.getIssuedAt());
            if (timeCompare != 0) {
                return timeCompare;
            }
        }

        // Tertiary: Special Assistance
        int assistCompare = Boolean.compare(t2.isSpecialAssistanceRequired(), t1.isSpecialAssistanceRequired());
        if (assistCompare != 0) {
            return assistCompare;
        }

        // Final deterministic tie-breaker
        if (t1.getTokenId() != null && t2.getTokenId() != null) {
            return t1.getTokenId().compareTo(t2.getTokenId());
        }

        return 0;
    }
}
