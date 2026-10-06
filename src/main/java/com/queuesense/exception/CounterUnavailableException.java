package com.queuesense.exception;

public class CounterUnavailableException extends RuntimeException {
    public CounterUnavailableException(String message) {
        super(message);
    }
}
