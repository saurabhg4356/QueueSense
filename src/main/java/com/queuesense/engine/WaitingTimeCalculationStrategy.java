package com.queuesense.engine;

import com.queuesense.model.Counter;
import com.queuesense.model.Token;

import java.util.List;

/**
 * Strategy interface for calculating dynamic waiting time estimations.
 */
public interface WaitingTimeCalculationStrategy {

    /**
     * Estimates waiting time in minutes for a specific token given people ahead and active counters.
     */
    int estimateWaitMinutes(Token token, int peopleAhead, List<Counter> compatibleCounters, double averageServiceMinutes);
}
