package com.queuesense.exception;

public class QueueEmptyException extends RuntimeException {
    public QueueEmptyException(String message) {
        super(message);
    }
}
