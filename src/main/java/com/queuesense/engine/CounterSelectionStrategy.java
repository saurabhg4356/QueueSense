package com.queuesense.engine;

import com.queuesense.model.Counter;
import com.queuesense.model.Token;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Strategy interface for selecting and ranking counters for token allocation.
 */
public interface CounterSelectionStrategy {

    /**
     * Recommends the optimal counter for the given token from available counters and workload map.
     */
    Optional<Counter> selectBestCounter(Token token, List<Counter> availableCounters, Map<Long, Integer> counterWorkloadMap);

    /**
     * Comparator for sorting counters from best to worst candidate.
     */
    Comparator<Counter> getCounterComparator(Map<Long, Integer> counterWorkloadMap);
}
