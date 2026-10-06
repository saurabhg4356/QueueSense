package com.queuesense.dto;

public class DashboardStatsDto {
    private int customersWaiting;
    private int customersServing;
    private int completedToday;
    private int cancelledToday;
    private int noShowsToday;
    private int activeCounters;
    private int totalCounters;
    private double averageWaitMinutes;
    private double averageServiceMinutes;
    private String peakHour;
    private double overallUtilizationPercentage;
    private double customerSatisfactionRating;

    public DashboardStatsDto() {}

    public int getCustomersWaiting() {
        return customersWaiting;
    }

    public void setCustomersWaiting(int customersWaiting) {
        this.customersWaiting = customersWaiting;
    }

    public int getCustomersServing() {
        return customersServing;
    }

    public void setCustomersServing(int customersServing) {
        this.customersServing = customersServing;
    }

    public int getCompletedToday() {
        return completedToday;
    }

    public void setCompletedToday(int completedToday) {
        this.completedToday = completedToday;
    }

    public int getCancelledToday() {
        return cancelledToday;
    }

    public void setCancelledToday(int cancelledToday) {
        this.cancelledToday = cancelledToday;
    }

    public int getNoShowsToday() {
        return noShowsToday;
    }

    public void setNoShowsToday(int noShowsToday) {
        this.noShowsToday = noShowsToday;
    }

    public int getActiveCounters() {
        return activeCounters;
    }

    public void setActiveCounters(int activeCounters) {
        this.activeCounters = activeCounters;
    }

    public int getTotalCounters() {
        return totalCounters;
    }

    public void setTotalCounters(int totalCounters) {
        this.totalCounters = totalCounters;
    }

    public double getAverageWaitMinutes() {
        return averageWaitMinutes;
    }

    public void setAverageWaitMinutes(double averageWaitMinutes) {
        this.averageWaitMinutes = averageWaitMinutes;
    }

    public double getAverageServiceMinutes() {
        return averageServiceMinutes;
    }

    public void setAverageServiceMinutes(double averageServiceMinutes) {
        this.averageServiceMinutes = averageServiceMinutes;
    }

    public String getPeakHour() {
        return peakHour;
    }

    public void setPeakHour(String peakHour) {
        this.peakHour = peakHour;
    }

    public double getOverallUtilizationPercentage() {
        return overallUtilizationPercentage;
    }

    public void setOverallUtilizationPercentage(double overallUtilizationPercentage) {
        this.overallUtilizationPercentage = overallUtilizationPercentage;
    }

    public double getCustomerSatisfactionRating() {
        return customerSatisfactionRating;
    }

    public void setCustomerSatisfactionRating(double customerSatisfactionRating) {
        this.customerSatisfactionRating = customerSatisfactionRating;
    }
}
