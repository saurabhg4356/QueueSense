package com.queuesense.exception;

import com.queuesense.enums.TokenStatus;

public class InvalidTokenStatusException extends RuntimeException {
    public InvalidTokenStatusException(String message) {
        super(message);
    }

    public InvalidTokenStatusException(TokenStatus fromStatus, TokenStatus toStatus) {
        super(String.format("Invalid token status transition from '%s' to '%s'. Transition is not permitted.",
                fromStatus != null ? fromStatus.name() : "NULL",
                toStatus != null ? toStatus.name() : "NULL"));
    }
}
