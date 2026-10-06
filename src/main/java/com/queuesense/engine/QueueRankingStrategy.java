package com.queuesense.engine;

import com.queuesense.model.AgingRule;
import com.queuesense.model.Token;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Strategy interface for ranking tokens within the Queue Decision Engine.
 */
public interface QueueRankingStrategy {
    
    /**
     * Calculates the dynamic effective score for a token at a given reference time.
     */
    double calculateScore(Token token, LocalDateTime referenceTime, AgingRule agingRule);

    /**
     * Returns a comparator for ordering tokens in a PriorityQueue.
     */
    Comparator<Token> getComparator(LocalDateTime referenceTime, AgingRule agingRule);
}
