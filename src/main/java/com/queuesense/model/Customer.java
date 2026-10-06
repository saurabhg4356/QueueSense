package com.queuesense.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {
    private Long customerId;
    private Long userId;
    private String customerNumber;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private boolean specialAssistanceRequired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer() {}

    public Customer(Long customerId, Long userId, String customerNumber, String fullName, String email, String phone, LocalDate dateOfBirth, boolean specialAssistanceRequired) {
        this.customerId = customerId;
        this.userId = userId;
        this.customerNumber = customerNumber;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.specialAssistanceRequired = specialAssistanceRequired;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isSpecialAssistanceRequired() {
        return specialAssistanceRequired;
    }

    public void setSpecialAssistanceRequired(boolean specialAssistanceRequired) {
        this.specialAssistanceRequired = specialAssistanceRequired;
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
        return "Customer{" +
                "customerId=" + customerId +
                ", customerNumber='" + customerNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", specialAssistance=" + specialAssistanceRequired +
                '}';
    }
}
