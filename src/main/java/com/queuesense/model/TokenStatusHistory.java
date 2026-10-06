package com.queuesense.model;

import com.queuesense.enums.TokenStatus;
import java.time.LocalDateTime;

public class TokenStatusHistory {
    private Long historyId;
    private Long tokenId;
    private TokenStatus oldStatus;
    private TokenStatus newStatus;
    private Long changedBy;
    private String changedByName;
    private String remarks;
    private LocalDateTime changedAt;

    public TokenStatusHistory() {}

    public TokenStatusHistory(Long tokenId, TokenStatus oldStatus, TokenStatus newStatus, Long changedBy, String remarks) {
        this.tokenId = tokenId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.remarks = remarks;
        this.changedAt = LocalDateTime.now();
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public TokenStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(TokenStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public TokenStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TokenStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Long changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
