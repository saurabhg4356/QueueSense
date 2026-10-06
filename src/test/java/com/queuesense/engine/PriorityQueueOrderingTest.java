package com.queuesense.engine;

import com.queuesense.enums.PriorityType;
import com.queuesense.model.AgingRule;
import com.queuesense.model.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriorityQueue Ordering & Algorithmic Scenarios Test")
public class PriorityQueueOrderingTest {

    private QueueDecisionEngine engine;
    private AgingRule agingRule;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        engine = new QueueDecisionEngine();
        agingRule = new AgingRule();
        agingRule.setMinutesInterval(10);
        agingRule.setBonusPoints(5);
        agingRule.setMaximumBonus(50);
        agingRule.setStarvationThresholdMinutes(30);
        agingRule.setSpecialAssistanceBonus(15);
        agingRule.setActive(true);

        baseTime = LocalDateTime.of(2026, 10, 6, 12, 0, 0);
    }

    @Test
    @DisplayName("Algorithm Test 1: NORMAL token waiting 50 minutes outranks PRIORITY token waiting 2 minutes due to anti-starvation aging")
    void testAntiStarvationAgingPrecedence() {
        Token normalStarved = new Token();
        normalStarved.setTokenId(1L);
        normalStarved.setTokenNumber("N-101");
        normalStarved.setPriorityType(PriorityType.NORMAL);
        normalStarved.setIssuedAt(baseTime.minusMinutes(50));
        normalStarved.setServicePriorityWeight(10);

        Token priorityRecent = new Token();
        priorityRecent.setTokenId(2L);
        priorityRecent.setTokenNumber("P-201");
        priorityRecent.setPriorityType(PriorityType.PRIORITY);
        priorityRecent.setIssuedAt(baseTime.minusMinutes(2));
        priorityRecent.setServicePriorityWeight(10);

        PriorityQueue<Token> pq = engine.buildPriorityQueue(Arrays.asList(priorityRecent, normalStarved), baseTime, agingRule);

        assertFalse(pq.isEmpty());
        Token firstServed = pq.poll();
        Token secondServed = pq.poll();

        assertEquals("N-101", firstServed.getTokenNumber(),
                "Customer waiting 50 minutes must be served before recent priority arrival to prevent starvation");
        assertEquals("P-201", secondServed.getTokenNumber());
    }

    @Test
    @DisplayName("Algorithm Test 3: Emergency token arriving immediately outranks normal token")
    void testEmergencyTokenOutranksNormal() {
        Token normalWaiting = new Token();
        normalWaiting.setTokenId(1L);
        normalWaiting.setTokenNumber("A-001");
        normalWaiting.setPriorityType(PriorityType.NORMAL);
        normalWaiting.setIssuedAt(baseTime.minusMinutes(10));
        normalWaiting.setServicePriorityWeight(10);

        Token emergencyArrival = new Token();
        emergencyArrival.setTokenId(2L);
        emergencyArrival.setTokenNumber("E-999");
        emergencyArrival.setPriorityType(PriorityType.EMERGENCY);
        emergencyArrival.setIssuedAt(baseTime); // just arrived
        emergencyArrival.setServicePriorityWeight(10);

        PriorityQueue<Token> pq = engine.buildPriorityQueue(Arrays.asList(normalWaiting, emergencyArrival), baseTime, agingRule);

        assertEquals("E-999", pq.poll().getTokenNumber(),
                "Emergency token should immediately be prioritized at the head of the queue");
    }

    @Test
    @DisplayName("FIFO Tie Breaker: Identical scores should prioritize the earlier arrival")
    void testFifoTieBreaker() {
        Token tokenFirstArrival = new Token();
        tokenFirstArrival.setTokenId(10L);
        tokenFirstArrival.setTokenNumber("T-01");
        tokenFirstArrival.setPriorityType(PriorityType.NORMAL);
        tokenFirstArrival.setIssuedAt(baseTime.minusMinutes(5));
        tokenFirstArrival.setServicePriorityWeight(10);

        Token tokenSecondArrival = new Token();
        tokenSecondArrival.setTokenId(11L);
        tokenSecondArrival.setTokenNumber("T-02");
        tokenSecondArrival.setPriorityType(PriorityType.NORMAL);
        tokenSecondArrival.setIssuedAt(baseTime.minusMinutes(5)); // same time
        tokenSecondArrival.setServicePriorityWeight(10);

        // Explicit distinct arrival times:
        tokenFirstArrival.setIssuedAt(baseTime.minusMinutes(6));
        tokenSecondArrival.setIssuedAt(baseTime.minusMinutes(5));

        TokenPriorityComparator comparator = new TokenPriorityComparator(baseTime, agingRule);
        assertTrue(comparator.compare(tokenFirstArrival, tokenSecondArrival) < 0,
                "Earlier issued token must be ordered before later token when scores match");
    }
}
