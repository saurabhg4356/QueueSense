package com.queuesense.model;

import com.queuesense.enums.PriorityType;

public class PriorityRule {
    private Long id;
    private PriorityType priorityType;
    private int baseWeight;
    private String description;
    private boolean active;

    public PriorityRule() {}

    public PriorityRule(Long id, PriorityType priorityType, int baseWeight, String description, boolean active) {
        this.id = id;
        this.priorityType = priorityType;
        this.baseWeight = baseWeight;
        this.description = description;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PriorityType getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(PriorityType priorityType) {
        this.priorityType = priorityType;
    }

    public int getBaseWeight() {
        return baseWeight;
    }

    public void setBaseWeight(int baseWeight) {
        this.baseWeight = baseWeight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
