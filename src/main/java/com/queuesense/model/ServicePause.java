package com.queuesense.model;

import com.queuesense.enums.PauseReason;
import java.time.LocalDateTime;

public class ServicePause {
    private Long pauseId;
    private Long sessionId;
    private PauseReason pauseReason;
    private LocalDateTime pausedAt;
    private LocalDateTime resumedAt;
    private int durationSeconds;
    private String remarks;

    public ServicePause() {}

    public ServicePause(Long sessionId, PauseReason pauseReason, String remarks) {
        this.sessionId = sessionId;
        this.pauseReason = pauseReason;
        this.remarks = remarks;
        this.pausedAt = LocalDateTime.now();
        this.durationSeconds = 0;
    }

    public Long getPauseId() {
        return pauseId;
    }

    public void setPauseId(Long pauseId) {
        this.pauseId = pauseId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public PauseReason getPauseReason() {
        return pauseReason;
    }

    public void setPauseReason(PauseReason pauseReason) {
        this.pauseReason = pauseReason;
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

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
