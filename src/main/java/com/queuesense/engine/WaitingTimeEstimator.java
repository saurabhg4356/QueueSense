package com.queuesense.engine;

import com.queuesense.model.Counter;
import com.queuesense.model.Token;

import java.util.List;

/**
 * WaitingTimeEstimator - Computes dynamic waiting time estimates based on queue depth,
 * service duration characteristics, and active counter capacity.
 */
public class WaitingTimeEstimator implements WaitingTimeCalculationStrategy {

    @Override
    public int estimateWaitMinutes(Token token, int peopleAhead, List<Counter> compatibleCounters, double averageServiceMinutes) {
        if (token == null || peopleAhead <= 0) {
            return 0;
        }

        // Determine effective service duration
        double serviceDuration = averageServiceMinutes > 0 ?
                averageServiceMinutes :
                (token.getServiceAverageMinutes() > 0 ? token.getServiceAverageMinutes() : 10.0);

        // Count actively operable counters (AVAILABLE or BUSY) capable of servicing this token
        long activeCounterCapacity = 0;
        if (compatibleCounters != null) {
            activeCounterCapacity = compatibleCounters.stream()
                    .filter(c -> c.getStatus().isOperable() && c.supportsService(token.getServiceId()))
                    .count();
        }

        // If no counters are currently active, assume minimal processing capacity of 1 counter
        long effectiveCapacity = Math.max(1, activeCounterCapacity);

        // Calculate parallelized expected wait time: (peopleAhead * duration) / effectiveCapacity
        double calculatedWait = (peopleAhead * serviceDuration) / (double) effectiveCapacity;

        return (int) Math.ceil(calculatedWait);
    }
}
