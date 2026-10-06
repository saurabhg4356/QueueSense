package com.queuesense.exception;

public class DuplicateTokenException extends RuntimeException {
    public DuplicateTokenException(String message) {
        super(message);
    }
}
