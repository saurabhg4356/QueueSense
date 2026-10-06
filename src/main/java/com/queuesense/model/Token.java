package com.queuesense.model;

import com.queuesense.enums.PriorityType;
import com.queuesense.enums.TokenStatus;
import java.time.Duration;
import java.time.LocalDateTime;

public class Token {
    private Long tokenId;
    private String tokenNumber;
    private Long customerId;
    private String customerName;
    private String customerNumber;
    private boolean specialAssistanceRequired;
    
    private Long serviceId;
    private String serviceName;
    private String serviceCode;
    private int serviceAverageMinutes;
    private int servicePriorityWeight;

    private PriorityType priorityType;
    private double priorityScore;
    private TokenStatus status;
    private Long counterId;
    private Integer counterNumber;
    private String counterName;

    private LocalDateTime issuedAt;
    private LocalDateTime calledAt;
    private LocalDateTime serviceStartedAt;
    private LocalDateTime serviceCompletedAt;
    private LocalDateTime cancelledAt;
    private Long cancelledBy;
    private String cancellationReason;
    private LocalDateTime noShowAt;

    private int estimatedWaitMinutes;
    private Integer actualWaitMinutes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Token() {}

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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public boolean isSpecialAssistanceRequired() {
        return specialAssistanceRequired;
    }

    public void setSpecialAssistanceRequired(boolean specialAssistanceRequired) {
        this.specialAssistanceRequired = specialAssistanceRequired;
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

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public int getServiceAverageMinutes() {
        return serviceAverageMinutes;
    }

    public void setServiceAverageMinutes(int serviceAverageMinutes) {
        this.serviceAverageMinutes = serviceAverageMinutes;
    }

    public int getServicePriorityWeight() {
        return servicePriorityWeight;
    }

    public void setServicePriorityWeight(int servicePriorityWeight) {
        this.servicePriorityWeight = servicePriorityWeight;
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

    public Long getCounterId() {
        return counterId;
    }

    public void setCounterId(Long counterId) {
        this.counterId = counterId;
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

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getCalledAt() {
        return calledAt;
    }

    public void setCalledAt(LocalDateTime calledAt) {
        this.calledAt = calledAt;
    }

    public LocalDateTime getServiceStartedAt() {
        return serviceStartedAt;
    }

    public void setServiceStartedAt(LocalDateTime serviceStartedAt) {
        this.serviceStartedAt = serviceStartedAt;
    }

    public LocalDateTime getServiceCompletedAt() {
        return serviceCompletedAt;
    }

    public void setServiceCompletedAt(LocalDateTime serviceCompletedAt) {
        this.serviceCompletedAt = serviceCompletedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Long getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(Long cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getNoShowAt() {
        return noShowAt;
    }

    public void setNoShowAt(LocalDateTime noShowAt) {
        this.noShowAt = noShowAt;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public Integer getActualWaitMinutes() {
        return actualWaitMinutes;
    }

    public void setActualWaitMinutes(Integer actualWaitMinutes) {
        this.actualWaitMinutes = actualWaitMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Calculates the elapsed waiting time in minutes from issuance to now (or service start).
     */
    public long getWaitingMinutesNow(LocalDateTime referenceTime) {
        if (issuedAt == null) {
            return 0;
        }
        LocalDateTime end = serviceStartedAt != null ? serviceStartedAt : (referenceTime != null ? referenceTime : LocalDateTime.now());
        if (end.isBefore(issuedAt)) {
            return 0;
        }
        return Duration.between(issuedAt, end).toMinutes();
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenId=" + tokenId +
                ", tokenNumber='" + tokenNumber + '\'' +
                ", status=" + status +
                ", priorityType=" + priorityType +
                ", priorityScore=" + priorityScore +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
