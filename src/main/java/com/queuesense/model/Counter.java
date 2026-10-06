package com.queuesense.model;

import com.queuesense.enums.CounterStatus;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Counter {
    private Long counterId;
    private int counterNumber;
    private String counterName;
    private String location;
    private CounterStatus status;
    private Long assignedAgentId;
    private String assignedAgentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Fast lookup for compatibility using HashSet
    private Set<Long> supportedServiceIds = new HashSet<>();

    public Counter() {}

    public Counter(Long counterId, int counterNumber, String counterName, String location, CounterStatus status, Long assignedAgentId) {
        this.counterId = counterId;
        this.counterNumber = counterNumber;
        this.counterName = counterName;
        this.location = location;
        this.status = status;
        this.assignedAgentId = assignedAgentId;
    }

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

    public Long getAssignedAgentId() {
        return assignedAgentId;
    }

    public void setAssignedAgentId(Long assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
    }

    public String getAssignedAgentName() {
        return assignedAgentName;
    }

    public void setAssignedAgentName(String assignedAgentName) {
        this.assignedAgentName = assignedAgentName;
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

    public Set<Long> getSupportedServiceIds() {
        return supportedServiceIds;
    }

    public void setSupportedServiceIds(Set<Long> supportedServiceIds) {
        this.supportedServiceIds = supportedServiceIds != null ? supportedServiceIds : new HashSet<>();
    }

    public boolean supportsService(Long serviceId) {
        return serviceId != null && supportedServiceIds.contains(serviceId);
    }

    public void addSupportedService(Long serviceId) {
        if (serviceId != null) {
            this.supportedServiceIds.add(serviceId);
        }
    }

    @Override
    public String toString() {
        return "Counter{" +
                "counterId=" + counterId +
                ", counterNumber=" + counterNumber +
                ", counterName='" + counterName + '\'' +
                ", status=" + status +
                ", supportedServicesCount=" + supportedServiceIds.size() +
                '}';
    }
}
