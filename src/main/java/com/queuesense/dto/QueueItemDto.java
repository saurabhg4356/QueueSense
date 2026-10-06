package com.queuesense.dto;

import com.queuesense.enums.PriorityType;
import com.queuesense.enums.TokenStatus;
import java.time.LocalDateTime;

public class QueueItemDto {
    private Long tokenId;
    private String tokenNumber;
    private String customerName;
    private String customerNumber;
    private boolean specialAssistance;
    private Long serviceId;
    private String serviceName;
    private PriorityType priorityType;
    private double priorityScore;
    private TokenStatus status;
    private Integer counterNumber;
    private String counterName;
    private int position;
    private int peopleAhead;
    private int estimatedWaitMinutes;
    private long waitingMinutesSoFar;
    private LocalDateTime issuedAt;

    public QueueItemDto() {}

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public boolean isSpecialAssistance() {
        return specialAssistance;
    }

    public void setSpecialAssistance(boolean specialAssistance) {
        this.specialAssistance = specialAssistance;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public PriorityType getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(PriorityType priorityType) {
        this.priorityType = priorityType;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public Integer getCounterNumber() {
        return counterNumber;
    }

    public void setCounterNumber(Integer counterNumber) {
        this.counterNumber = counterNumber;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPeopleAhead() {
        return peopleAhead;
    }

    public void setPeopleAhead(int peopleAhead) {
        this.peopleAhead = peopleAhead;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public long getWaitingMinutesSoFar() {
        return waitingMinutesSoFar;
    }

    public void setWaitingMinutesSoFar(long waitingMinutesSoFar) {
        this.waitingMinutesSoFar = waitingMinutesSoFar;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
