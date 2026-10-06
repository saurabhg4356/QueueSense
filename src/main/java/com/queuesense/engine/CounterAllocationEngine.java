package com.queuesense.engine;

import com.queuesense.enums.CounterStatus;
import com.queuesense.model.Counter;
import com.queuesense.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * CounterAllocationEngine - Ranks and allocates counters based on compatibility, availability, and workload.
 *
 * Employs:
 * - HashSet for constant-time compatibility verification
 * - HashMap for workload aggregation per counter
 * - Custom Comparator for ranking counters by operational readiness
 */
public class CounterAllocationEngine implements CounterSelectionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CounterAllocationEngine.class);

    @Override
    public Optional<Counter> selectBestCounter(Token token, List<Counter> availableCounters, Map<Long, Integer> counterWorkloadMap) {
        if (token == null || availableCounters == null || availableCounters.isEmpty()) {
            return Optional.empty();
        }

        // 1. Filter only operable counters (AVAILABLE or BUSY) that support this token's service
        List<Counter> eligibleCounters = new ArrayList<>();
        for (Counter c : availableCounters) {
            if (c.getStatus().isOperable() && c.supportsService(token.getServiceId())) {
                eligibleCounters.add(c);
            }
        }

        if (eligibleCounters.isEmpty()) {
            logger.warn("No operable compatible counter found for service ID: {}", token.getServiceId());
            return Optional.empty();
        }

        // 2. Sort eligible counters using workload and availability comparator
        eligibleCounters.sort(getCounterComparator(counterWorkloadMap));

        Counter bestCounter = eligibleCounters.get(0);
        logger.info("Allocated best Counter {} [Status: {}, Workload: {}] for Token {}",
                bestCounter.getCounterName(), bestCounter.getStatus(),
                counterWorkloadMap != null ? counterWorkloadMap.getOrDefault(bestCounter.getCounterId(), 0) : 0,
                token.getTokenNumber());

        return Optional.of(bestCounter);
    }

    @Override
    public Comparator<Counter> getCounterComparator(Map<Long, Integer> counterWorkloadMap) {
        return (c1, c2) -> {
            if (c1 == c2) return 0;
            if (c1 == null) return 1;
            if (c2 == null) return -1;

            // 1. Status priority: AVAILABLE > BUSY > BREAK > OFFLINE
            int statusPriority1 = getStatusPriority(c1.getStatus());
            int statusPriority2 = getStatusPriority(c2.getStatus());
            if (statusPriority1 != statusPriority2) {
                return Integer.compare(statusPriority1, statusPriority2);
            }

            // 2. Workload comparison: Lower workload is preferred
            int load1 = (counterWorkloadMap != null) ? counterWorkloadMap.getOrDefault(c1.getCounterId(), 0) : 0;
            int load2 = (counterWorkloadMap != null) ? counterWorkloadMap.getOrDefault(c2.getCounterId(), 0) : 0;
            int loadCompare = Integer.compare(load1, load2);
            if (loadCompare != 0) {
                return loadCompare;
            }

            // 3. Counter Number tie-breaker
            return Integer.compare(c1.getCounterNumber(), c2.getCounterNumber());
        };
    }

    private int getStatusPriority(CounterStatus status) {
        if (status == null) return 99;
        return switch (status) {
            case AVAILABLE -> 1;
            case BUSY -> 2;
            case BREAK -> 3;
            case OFFLINE -> 4;
        };
    }
}
