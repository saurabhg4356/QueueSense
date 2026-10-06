package com.queuesense.dto;

import com.queuesense.enums.CounterStatus;

public class CounterWorkloadDto {
    private Long counterId;
    private int counterNumber;
    private String counterName;
    private String location;
    private CounterStatus status;
    private String agentName;
    private String currentServingToken;
    private int waitingTokensCount;
    private int totalAssignedToday;
    private double workloadPercentage;
    private boolean recommended;
    private String recommendationReason;

    public CounterWorkloadDto() {}

    public Long getCounterId() {
        return counterId;
    }

    public void setCounterId(Long counterId) {
        this.counterId = counterId;
    }

    public int getCounterNumber() {
        return counterNumber;
    }

    public void setCounterNumber(int counterNumber) {
        this.counterNumber = counterNumber;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public CounterStatus getStatus() {
        return status;
    }

    public void setStatus(CounterStatus status) {
        this.status = status;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getCurrentServingToken() {
        return currentServingToken;
    }

    public void setCurrentServingToken(String currentServingToken) {
        this.currentServingToken = currentServingToken;
    }

    public int getWaitingTokensCount() {
        return waitingTokensCount;
    }

    public void setWaitingTokensCount(int waitingTokensCount) {
        this.waitingTokensCount = waitingTokensCount;
    }

    public int getTotalAssignedToday() {
        return totalAssignedToday;
    }

    public void setTotalAssignedToday(int totalAssignedToday) {
        this.totalAssignedToday = totalAssignedToday;
    }

    public double getWorkloadPercentage() {
        return workloadPercentage;
    }

    public void setWorkloadPercentage(double workloadPercentage) {
        this.workloadPercentage = workloadPercentage;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }
}
