package com.queuesense.exception;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String message) {
        super(message);
    }

    public TokenNotFoundException(Long tokenId) {
        super("Token with ID " + tokenId + " was not found.");
    }
}
