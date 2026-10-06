package com.queuesense.engine;

import com.queuesense.enums.PriorityType;
import com.queuesense.model.AgingRule;
import com.queuesense.model.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Priority Calculator & Anti-Starvation Unit Tests")
public class PriorityCalculatorTest {

    private AgingRule agingRule;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        agingRule = new AgingRule();
        agingRule.setMinutesInterval(10);
        agingRule.setBonusPoints(5);
        agingRule.setMaximumBonus(50);
        agingRule.setStarvationThresholdMinutes(30);
        agingRule.setSpecialAssistanceBonus(15);
        agingRule.setActive(true);

        baseTime = LocalDateTime.of(2026, 10, 6, 10, 0, 0);
    }

    @Test
    @DisplayName("Should accurately calculate base priority weight for NORMAL, PRIORITY, URGENT, EMERGENCY")
    void testBasePriorityCalculations() {
        Token normal = createToken(PriorityType.NORMAL, 0, false, 0);
        Token priority = createToken(PriorityType.PRIORITY, 0, false, 0);
        Token urgent = createToken(PriorityType.URGENT, 0, false, 0);
        Token emergency = createToken(PriorityType.EMERGENCY, 0, false, 0);

        assertEquals(10.0, PriorityCalculator.calculateScore(normal, baseTime, agingRule));
        assertEquals(30.0, PriorityCalculator.calculateScore(priority, baseTime, agingRule));
        assertEquals(60.0, PriorityCalculator.calculateScore(urgent, baseTime, agingRule));
        assertEquals(100.0, PriorityCalculator.calculateScore(emergency, baseTime, agingRule));
    }

    @Test
    @DisplayName("Should award incremental aging bonus per configured minutes interval")
    void testAgingBonusIncrements() {
        // At 0 mins: bonus = 0
        assertEquals(0.0, PriorityCalculator.calculateAgingBonus(0, agingRule));
        // At 9 mins (less than 10 min interval): bonus = 0
        assertEquals(0.0, PriorityCalculator.calculateAgingBonus(9, agingRule));
        // At 10 mins: 1 interval * 5 = 5
        assertEquals(5.0, PriorityCalculator.calculateAgingBonus(10, agingRule));
        // At 20 mins: 2 intervals * 5 = 10
        assertEquals(10.0, PriorityCalculator.calculateAgingBonus(20, agingRule));
        // At 25 mins: still 2 intervals * 5 = 10
        assertEquals(10.0, PriorityCalculator.calculateAgingBonus(25, agingRule));
    }

    @Test
    @DisplayName("Aging bonus must NOT exceed maximum bonus cap")
    void testAgingBonusCap() {
        // At 150 mins waiting: 15 intervals * 5 = 75, but cap is 50
        double bonus = PriorityCalculator.calculateAgingBonus(150, agingRule);
        assertEquals(50.0, bonus, "Aging bonus must not exceed configured maximum bonus");
    }

    @Test
    @DisplayName("Should apply special assistance bonus to customers with assistance requirement")
    void testSpecialAssistanceBonus() {
        Token regular = createToken(PriorityType.NORMAL, 0, false, 0);
        Token assisted = createToken(PriorityType.NORMAL, 0, true, 0);

        double regularScore = PriorityCalculator.calculateScore(regular, baseTime, agingRule);
        double assistedScore = PriorityCalculator.calculateScore(assisted, baseTime, agingRule);

        assertEquals(regularScore + 15.0, assistedScore, "Assisted customer should receive configured assistance bonus");
    }

    @Test
    @DisplayName("Should trigger starvation fairness boost when waiting minutes exceeds threshold")
    void testStarvationFairnessBoost() {
        Token normalUnderThreshold = createToken(PriorityType.NORMAL, 20, false, 0);
        Token normalPastThreshold = createToken(PriorityType.NORMAL, 40, false, 0);

        double score20 = PriorityCalculator.calculateScore(normalUnderThreshold, baseTime, agingRule);
        double score40 = PriorityCalculator.calculateScore(normalPastThreshold, baseTime, agingRule);

        // Score at 20 min: 10 base + (20 * 0.5) wait + 10 aging = 30.0
        assertEquals(30.0, score20);

        // Score at 40 min: 10 base + (40 * 0.5) wait + 20 aging + [10 + (10 * 0.5)] starvation boost = 65.0
        assertTrue(score40 >= 60.0, "Customer waiting 40 min should receive significant anti-starvation boost");
    }

    private Token createToken(PriorityType type, int waitMinutesBefore, boolean specialAssistance, int serviceWeight) {
        Token t = new Token();
        t.setPriorityType(type);
        t.setIssuedAt(baseTime.minusMinutes(waitMinutesBefore));
        t.setSpecialAssistanceRequired(specialAssistance);
        t.setServicePriorityWeight(serviceWeight);
        return t;
    }
}
