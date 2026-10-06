package com.queuesense.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle states of a queue token.
 * Enforces valid transition paths to prevent state corruption.
 */
public enum TokenStatus {
    WAITING("Waiting"),
    CALLED("Called"),
    SERVING("Serving"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NO_SHOW("No-Show"),
    TRANSFERRED("Transferred");

    private final String displayName;

    TokenStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if transitioning from this status to targetStatus is legally permitted by business rules.
     */
    public boolean canTransitionTo(TokenStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        if (this == targetStatus) {
            return true;
        }

        return switch (this) {
            case WAITING -> targetStatus == CALLED || targetStatus == CANCELLED;
            case CALLED -> targetStatus == SERVING || targetStatus == NO_SHOW || targetStatus == CANCELLED;
            case SERVING -> targetStatus == PAUSED || targetStatus == COMPLETED || targetStatus == TRANSFERRED;
            case PAUSED -> targetStatus == SERVING || targetStatus == COMPLETED || targetStatus == TRANSFERRED;
            case COMPLETED, CANCELLED, NO_SHOW, TRANSFERRED -> false; // Terminal states
        };
    }

    public Set<TokenStatus> getAllowedNextStatuses() {
        return switch (this) {
            case WAITING -> EnumSet.of(CALLED, CANCELLED);
            case CALLED -> EnumSet.of(SERVING, NO_SHOW, CANCELLED);
            case SERVING -> EnumSet.of(PAUSED, COMPLETED, TRANSFERRED);
            case PAUSED -> EnumSet.of(SERVING, COMPLETED, TRANSFERRED);
            default -> Collections.emptySet();
        };
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == NO_SHOW || this == TRANSFERRED;
    }
}
