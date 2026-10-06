package com.queuesense.enums;

public enum CounterStatus {
    AVAILABLE("Available"),
    BUSY("Busy"),
    BREAK("On Break"),
    OFFLINE("Offline");

    private final String displayName;

    CounterStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isOperable() {
        return this == AVAILABLE || this == BUSY;
    }
}
