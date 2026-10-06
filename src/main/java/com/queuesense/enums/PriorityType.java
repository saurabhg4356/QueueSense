package com.queuesense.enums;

public enum PriorityType {
    NORMAL("Normal", 10),
    PRIORITY("Priority", 30),
    URGENT("Urgent", 60),
    EMERGENCY("Emergency", 100);

    private final String label;
    private final int defaultWeight;

    PriorityType(String label, int defaultWeight) {
        this.label = label;
        this.defaultWeight = defaultWeight;
    }

    public String getLabel() {
        return label;
    }

    public int getDefaultWeight() {
        return defaultWeight;
    }
}
