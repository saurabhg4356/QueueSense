package com.queuesense.model;

import java.time.LocalDateTime;

public class Service {
    private Long serviceId;
    private String serviceCode;
    private String serviceName;
    private String description;
    private int averageServiceMinutes;
    private int priorityWeight;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Service() {}

    public Service(Long serviceId, String serviceCode, String serviceName, String description, int averageServiceMinutes, int priorityWeight, boolean active) {
        this.serviceId = serviceId;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.description = description;
        this.averageServiceMinutes = averageServiceMinutes;
        this.priorityWeight = priorityWeight;
        this.active = active;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAverageServiceMinutes() {
        return averageServiceMinutes;
    }

    public void setAverageServiceMinutes(int averageServiceMinutes) {
        this.averageServiceMinutes = averageServiceMinutes;
    }

    public int getPriorityWeight() {
        return priorityWeight;
    }

    public void setPriorityWeight(int priorityWeight) {
        this.priorityWeight = priorityWeight;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    @Override
    public String toString() {
        return "Service{" +
                "serviceId=" + serviceId +
                ", serviceCode='" + serviceCode + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", avgMinutes=" + averageServiceMinutes +
                ", priorityWeight=" + priorityWeight +
                ", active=" + active +
                '}';
    }
}
