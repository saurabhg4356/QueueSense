package com.queuesense.exception;

public class InvalidServiceSessionException extends RuntimeException {
    public InvalidServiceSessionException(String message) {
        super(message);
    }
}
