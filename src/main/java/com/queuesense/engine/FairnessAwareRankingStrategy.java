package com.queuesense.engine;

import com.queuesense.model.AgingRule;
import com.queuesense.model.Token;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Default implementation of QueueRankingStrategy incorporating aging and anti-starvation.
 */
public class FairnessAwareRankingStrategy implements QueueRankingStrategy {

    @Override
    public double calculateScore(Token token, LocalDateTime referenceTime, AgingRule agingRule) {
        return PriorityCalculator.calculateScore(token, referenceTime, agingRule);
    }

    @Override
    public Comparator<Token> getComparator(LocalDateTime referenceTime, AgingRule agingRule) {
        return new TokenPriorityComparator(referenceTime, agingRule);
    }
}
