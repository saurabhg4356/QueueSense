package com.queuesense.model;

import com.queuesense.enums.TokenStatus;
import com.queuesense.exception.InvalidTokenStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Token Lifecycle & Status Transition Tests")
public class TokenStatusTransitionTest {

    @Test
    @DisplayName("Should permit valid lifecycle paths through the operational workflow")
    void testPermittedTransitions() {
        assertTrue(TokenStatus.WAITING.canTransitionTo(TokenStatus.CALLED));
        assertTrue(TokenStatus.WAITING.canTransitionTo(TokenStatus.CANCELLED));

        assertTrue(TokenStatus.CALLED.canTransitionTo(TokenStatus.SERVING));
        assertTrue(TokenStatus.CALLED.canTransitionTo(TokenStatus.NO_SHOW));
        assertTrue(TokenStatus.CALLED.canTransitionTo(TokenStatus.CANCELLED));

        assertTrue(TokenStatus.SERVING.canTransitionTo(TokenStatus.PAUSED));
        assertTrue(TokenStatus.SERVING.canTransitionTo(TokenStatus.COMPLETED));
        assertTrue(TokenStatus.SERVING.canTransitionTo(TokenStatus.TRANSFERRED));

        assertTrue(TokenStatus.PAUSED.canTransitionTo(TokenStatus.SERVING));
        assertTrue(TokenStatus.PAUSED.canTransitionTo(TokenStatus.COMPLETED));
    }

    @Test
    @DisplayName("Should block illegal status transitions and enforce terminal states")
    void testIllegalTransitions() {
        assertFalse(TokenStatus.WAITING.canTransitionTo(TokenStatus.COMPLETED));
        assertFalse(TokenStatus.WAITING.canTransitionTo(TokenStatus.SERVING));
        assertFalse(TokenStatus.COMPLETED.canTransitionTo(TokenStatus.WAITING));
        assertFalse(TokenStatus.CANCELLED.canTransitionTo(TokenStatus.CALLED));
        assertFalse(TokenStatus.NO_SHOW.canTransitionTo(TokenStatus.SERVING));
    }

    @Test
    @DisplayName("InvalidTokenStatusException should report both from and to states accurately")
    void testExceptionFormatting() {
        InvalidTokenStatusException ex = new InvalidTokenStatusException(TokenStatus.WAITING, TokenStatus.COMPLETED);
        assertTrue(ex.getMessage().contains("WAITING"));
        assertTrue(ex.getMessage().contains("COMPLETED"));
    }
}
