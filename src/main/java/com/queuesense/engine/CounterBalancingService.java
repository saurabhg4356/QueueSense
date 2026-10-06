package com.queuesense.engine;

import com.queuesense.dto.CounterWorkloadDto;
import com.queuesense.enums.CounterStatus;
import com.queuesense.model.Counter;
import com.queuesense.model.Token;

import java.util.*;

/**
 * CounterBalancingService - Evaluates operational distribution across all counters,
 * identifies load imbalances, and provides real-time workload balancing suggestions.
 */
public class CounterBalancingService {

    public List<CounterWorkloadDto> evaluateCounterWorkloads(List<Counter> counters,
                                                             List<Token> waitingTokens,
                                                             Map<Long, Token> activeServingTokenMap) {
        List<CounterWorkloadDto> dtos = new ArrayList<>();
        if (counters == null || counters.isEmpty()) {
            return dtos;
        }

        // 1. Calculate waiting tokens compatible with each counter using HashMap
        Map<Long, Integer> waitingPerCounter = new HashMap<>();
        for (Counter c : counters) {
            int count = 0;
            if (waitingTokens != null) {
                for (Token t : waitingTokens) {
                    if (c.supportsService(t.getServiceId())) {
                        count++;
                    }
                }
            }
            waitingPerCounter.put(c.getCounterId(), count);
        }

        // 2. Identify min and max workload among operable counters
        int maxLoad = -1;
        int minLoad = Integer.MAX_VALUE;
        Long mostLoadedCounterId = null;
        Long leastLoadedCounterId = null;

        for (Counter c : counters) {
            if (c.getStatus().isOperable()) {
                int load = waitingPerCounter.getOrDefault(c.getCounterId(), 0);
                if (activeServingTokenMap != null && activeServingTokenMap.containsKey(c.getCounterId())) {
                    load += 1;
                }
                if (load > maxLoad) {
                    maxLoad = load;
                    mostLoadedCounterId = c.getCounterId();
                }
                if (load < minLoad) {
                    minLoad = load;
                    leastLoadedCounterId = c.getCounterId();
                }
            }
        }

        // 3. Populate DTOs with balancing recommendations
        for (Counter c : counters) {
            CounterWorkloadDto dto = new CounterWorkloadDto();
            dto.setCounterId(c.getCounterId());
            dto.setCounterNumber(c.getCounterNumber());
            dto.setCounterName(c.getCounterName());
            dto.setLocation(c.getLocation());
            dto.setStatus(c.getStatus());
            dto.setAgentName(c.getAssignedAgentName() != null ? c.getAssignedAgentName() : "Unassigned");

            Token servingToken = (activeServingTokenMap != null) ? activeServingTokenMap.get(c.getCounterId()) : null;
            dto.setCurrentServingToken(servingToken != null ? servingToken.getTokenNumber() + " (" + servingToken.getServiceName() + ")" : "None");

            int waitingCount = waitingPerCounter.getOrDefault(c.getCounterId(), 0);
            dto.setWaitingTokensCount(waitingCount);

            int currentLoad = waitingCount + (servingToken != null ? 1 : 0);
            double pct = (maxLoad > 0) ? Math.round(((double) currentLoad / maxLoad) * 100.0) : 0.0;
            dto.setWorkloadPercentage(pct);

            if (c.getStatus() == CounterStatus.AVAILABLE && servingToken == null) {
                dto.setRecommended(true);
                dto.setRecommendationReason("Counter is idle and immediately available for next eligible customer.");
            } else if (c.getCounterId().equals(leastLoadedCounterId) && maxLoad > minLoad + 2) {
                dto.setRecommended(true);
                dto.setRecommendationReason("Underutilized counter: Recommended target for incoming routing or transfer.");
            } else if (c.getCounterId().equals(mostLoadedCounterId) && maxLoad >= 4) {
                dto.setRecommended(false);
                dto.setRecommendationReason("High workload detected. Consider opening additional compatible counters.");
            } else {
                dto.setRecommended(false);
                dto.setRecommendationReason("Normal operational load.");
            }

            dtos.add(dto);
        }

        return dtos;
    }
}
