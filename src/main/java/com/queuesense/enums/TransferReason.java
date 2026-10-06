package com.queuesense.enums;

public enum TransferReason {
    SPECIALIZED_SERVICE_REQUIRED("Specialized Service Required"),
    AGENT_UNAVAILABLE("Agent Unavailable"),
    COUNTER_CLOSING("Counter Closing"),
    TECHNICAL_PROBLEM("Technical Problem"),
    CUSTOMER_REQUEST("Customer Request"),
    OTHER("Other");

    private final String label;

    TransferReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
