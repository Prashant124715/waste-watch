package com.wastewatch.dto.violation;

import com.wastewatch.entity.enums.ViolationStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ViolationStatusUpdateDto {

    @NotNull(message = "Violation status is required")
    private ViolationStatus status;

    private String resolutionNotes;

    private BigDecimal fineAmount;

    public ViolationStatusUpdateDto() {
    }

    public ViolationStatus getStatus() {
        return status;
    }

    public void setStatus(ViolationStatus status) {
        this.status = status;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }
}
