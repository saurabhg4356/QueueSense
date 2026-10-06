package com.queuesense.engine;

import com.queuesense.enums.CounterStatus;
import com.queuesense.model.Counter;
import com.queuesense.model.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Counter Allocation Engine & Workload Ranking Test")
public class CounterAllocationEngineTest {

    private CounterAllocationEngine allocationEngine;
    private Token loanToken;

    @BeforeEach
    void setUp() {
        allocationEngine = new CounterAllocationEngine();
        loanToken = new Token();
        loanToken.setTokenId(101L);
        loanToken.setTokenNumber("L-101");
        loanToken.setServiceId(4L); // LOAN_DOCUMENTATION
    }

    @Test
    @DisplayName("Algorithm Test 2: When two counters are compatible, counter with lower workload is ranked higher")
    void testLowerWorkloadRankedHigher() {
        Counter counterA = new Counter(1L, 1, "Counter 1", "Ground Floor", CounterStatus.AVAILABLE, null);
        counterA.addSupportedService(4L);

        Counter counterB = new Counter(2L, 2, "Counter 2", "Ground Floor", CounterStatus.AVAILABLE, null);
        counterB.addSupportedService(4L);

        Map<Long, Integer> workloadMap = new HashMap<>();
        workloadMap.put(1L, 8); // Counter A has 8 waiting tokens
        workloadMap.put(2L, 2); // Counter B has 2 waiting tokens

        Optional<Counter> best = allocationEngine.selectBestCounter(loanToken, Arrays.asList(counterA, counterB), workloadMap);

        assertTrue(best.isPresent());
        assertEquals(2L, best.get().getCounterId(), "Counter B with 2 waiting tokens must be selected over Counter A with 8 tokens");
    }

    @Test
    @DisplayName("Should exclude offline and on-break counters even if service compatible")
    void testExcludeUnavailableCounters() {
        Counter offlineCounter = new Counter(1L, 1, "Counter 1", "Wing A", CounterStatus.OFFLINE, null);
        offlineCounter.addSupportedService(4L);

        Counter breakCounter = new Counter(2L, 2, "Counter 2", "Wing A", CounterStatus.BREAK, null);
        breakCounter.addSupportedService(4L);

        Counter availableCounter = new Counter(3L, 3, "Counter 3", "Wing A", CounterStatus.AVAILABLE, null);
        availableCounter.addSupportedService(4L);

        Map<Long, Integer> workloadMap = new HashMap<>();
        workloadMap.put(3L, 1);

        Optional<Counter> best = allocationEngine.selectBestCounter(loanToken, Arrays.asList(offlineCounter, breakCounter, availableCounter), workloadMap);

        assertTrue(best.isPresent());
        assertEquals(3L, best.get().getCounterId());
    }

    @Test
    @DisplayName("Algorithm Test 5: Returns empty when no compatible counter exists")
    void testNoCompatibleCounterReturnsEmpty() {
        Counter tellerCounter = new Counter(1L, 1, "Counter 1", "Wing A", CounterStatus.AVAILABLE, null);
        tellerCounter.addSupportedService(2L); // Supports CASH_DEPOSIT, NOT LOANS (4)

        Optional<Counter> best = allocationEngine.selectBestCounter(loanToken, Collections.singletonList(tellerCounter), Collections.emptyMap());

        assertTrue(best.isEmpty(), "Should return empty when no counter supports requested service");
    }
}
