package com.queuesense.model;

import java.time.LocalDateTime;

public class ServiceSession {
    private Long sessionId;
    private Long tokenId;
    private Long counterId;
    private LocalDateTime startedAt;
    private LocalDateTime pausedAt;
    private LocalDateTime resumedAt;
    private LocalDateTime completedAt;
    private int actualDurationSeconds;
    private String remarks;

    public ServiceSession() {}

    public ServiceSession(Long tokenId, Long counterId) {
        this.tokenId = tokenId;
        this.counterId = counterId;
        this.startedAt = LocalDateTime.now();
        this.actualDurationSeconds = 0;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public Long getCounterId() {
        return counterId;
    }

    public void setCounterId(Long counterId) {
        this.counterId = counterId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(LocalDateTime pausedAt) {
        this.pausedAt = pausedAt;
    }

    public LocalDateTime getResumedAt() {
        return resumedAt;
    }

    public void setResumedAt(LocalDateTime resumedAt) {
        this.resumedAt = resumedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getActualDurationSeconds() {
        return actualDurationSeconds;
    }

    public void setActualDurationSeconds(int actualDurationSeconds) {
        this.actualDurationSeconds = actualDurationSeconds;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
