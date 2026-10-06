package com.queuesense.engine;

import com.queuesense.enums.CounterStatus;
import com.queuesense.model.Counter;
import com.queuesense.model.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Waiting Time Estimator Tests")
public class WaitingTimeEstimatorTest {

    private WaitingTimeEstimator estimator;
    private Token token;

    @BeforeEach
    void setUp() {
        estimator = new WaitingTimeEstimator();
        token = new Token();
        token.setServiceId(1L);
        token.setServiceAverageMinutes(10);
    }

    @Test
    @DisplayName("Should estimate 0 minutes when customer is first in line (0 people ahead)")
    void testZeroPeopleAheadEstimatesZero() {
        assertEquals(0, estimator.estimateWaitMinutes(token, 0, Collections.emptyList(), 10.0));
    }

    @Test
    @DisplayName("Should divide waiting time by active compatible counters")
    void testParallelCounterCapacityScaling() {
        Counter c1 = new Counter(1L, 1, "Counter 1", "Wing A", CounterStatus.AVAILABLE, null);
        c1.addSupportedService(1L);
        Counter c2 = new Counter(2L, 2, "Counter 2", "Wing A", CounterStatus.BUSY, null);
        c2.addSupportedService(1L);

        // 6 people ahead, 2 active counters, average service 10 mins:
        // Expected wait = (6 * 10) / 2 = 30 minutes
        int estimate = estimator.estimateWaitMinutes(token, 6, Arrays.asList(c1, c2), 10.0);
        assertEquals(30, estimate);
    }
}
