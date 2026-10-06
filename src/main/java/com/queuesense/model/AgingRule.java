package com.queuesense.model;

public class AgingRule {
    private Long id;
    private int minutesInterval;
    private int bonusPoints;
    private int maximumBonus;
    private int starvationThresholdMinutes;
    private int specialAssistanceBonus;
    private boolean active;

    public AgingRule() {
        this.minutesInterval = 10;
        this.bonusPoints = 5;
        this.maximumBonus = 50;
        this.starvationThresholdMinutes = 30;
        this.specialAssistanceBonus = 15;
        this.active = true;
    }

    public AgingRule(Long id, int minutesInterval, int bonusPoints, int maximumBonus, int starvationThresholdMinutes, int specialAssistanceBonus, boolean active) {
        this.id = id;
        this.minutesInterval = minutesInterval;
        this.bonusPoints = bonusPoints;
        this.maximumBonus = maximumBonus;
        this.starvationThresholdMinutes = starvationThresholdMinutes;
        this.specialAssistanceBonus = specialAssistanceBonus;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMinutesInterval() {
        return minutesInterval;
    }

    public void setMinutesInterval(int minutesInterval) {
        this.minutesInterval = minutesInterval;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(int bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public int getMaximumBonus() {
        return maximumBonus;
    }

    public void setMaximumBonus(int maximumBonus) {
        this.maximumBonus = maximumBonus;
    }

    public int getStarvationThresholdMinutes() {
        return starvationThresholdMinutes;
    }

    public void setStarvationThresholdMinutes(int starvationThresholdMinutes) {
        this.starvationThresholdMinutes = starvationThresholdMinutes;
    }

    public int getSpecialAssistanceBonus() {
        return specialAssistanceBonus;
    }

    public void setSpecialAssistanceBonus(int specialAssistanceBonus) {
        this.specialAssistanceBonus = specialAssistanceBonus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
