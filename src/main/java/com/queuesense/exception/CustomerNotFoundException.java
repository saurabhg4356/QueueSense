package com.queuesense.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(Long customerId) {
        super("Customer with ID " + customerId + " was not found.");
    }
}
