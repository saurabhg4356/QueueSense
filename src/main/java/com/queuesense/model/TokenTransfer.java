package com.queuesense.model;

import com.queuesense.enums.TransferReason;
import java.time.LocalDateTime;

public class TokenTransfer {
    private Long transferId;
    private Long tokenId;
    private Long fromCounterId;
    private Integer fromCounterNumber;
    private Long toCounterId;
    private Integer toCounterNumber;
    private TransferReason reason;
    private Long transferredBy;
    private String transferredByName;
    private LocalDateTime transferredAt;
    private String remarks;

    public TokenTransfer() {}

    public TokenTransfer(Long tokenId, Long fromCounterId, Long toCounterId, TransferReason reason, Long transferredBy, String remarks) {
        this.tokenId = tokenId;
        this.fromCounterId = fromCounterId;
        this.toCounterId = toCounterId;
        this.reason = reason;
        this.transferredBy = transferredBy;
        this.remarks = remarks;
        this.transferredAt = LocalDateTime.now();
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public Long getFromCounterId() {
        return fromCounterId;
    }

    public void setFromCounterId(Long fromCounterId) {
        this.fromCounterId = fromCounterId;
    }

    public Integer getFromCounterNumber() {
        return fromCounterNumber;
    }

    public void setFromCounterNumber(Integer fromCounterNumber) {
        this.fromCounterNumber = fromCounterNumber;
    }

    public Long getToCounterId() {
        return toCounterId;
    }

    public void setToCounterId(Long toCounterId) {
        this.toCounterId = toCounterId;
    }

    public Integer getToCounterNumber() {
        return toCounterNumber;
    }

    public void setToCounterNumber(Integer toCounterNumber) {
        this.toCounterNumber = toCounterNumber;
    }

    public TransferReason getReason() {
        return reason;
    }

    public void setReason(TransferReason reason) {
        this.reason = reason;
    }

    public Long getTransferredBy() {
        return transferredBy;
    }

    public void setTransferredBy(Long transferredBy) {
        this.transferredBy = transferredBy;
    }

    public String getTransferredByName() {
        return transferredByName;
    }

    public void setTransferredByName(String transferredByName) {
        this.transferredByName = transferredByName;
    }

    public LocalDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(LocalDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
