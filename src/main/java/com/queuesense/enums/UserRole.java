package com.queuesense.enums;

public enum UserRole {
    ADMIN("Administrator"),
    OPERATOR("Desk Operator"),
    COUNTER_AGENT("Counter Agent"),
    CUSTOMER("Customer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
