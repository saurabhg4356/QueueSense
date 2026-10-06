package com.queuesense.exception;

public class CounterNotFoundException extends RuntimeException {
    public CounterNotFoundException(String message) {
        super(message);
    }

    public CounterNotFoundException(Long counterId) {
        super("Counter with ID " + counterId + " was not found.");
    }
}
