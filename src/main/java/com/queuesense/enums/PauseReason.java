package com.queuesense.enums;

public enum PauseReason {
    DOCUMENT_VERIFICATION("Document Verification"),
    CUSTOMER_REQUEST("Customer Requested Time"),
    SUPERVISOR_APPROVAL("Supervisor Approval Needed"),
    SYSTEM_ISSUE("System / Hardware Issue"),
    OTHER("Other Operational Delay");

    private final String label;

    PauseReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
