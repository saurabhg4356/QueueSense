package com.queuesense.exception;

public class ServiceNotSupportedException extends RuntimeException {
    public ServiceNotSupportedException(String message) {
        super(message);
    }

    public ServiceNotSupportedException(Long counterId, Long serviceId) {
        super(String.format("Counter ID %d does not support Service ID %d.", counterId, serviceId));
    }
}
