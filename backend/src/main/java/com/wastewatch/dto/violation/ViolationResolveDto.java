package com.wastewatch.dto.violation;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Request DTO for resolving a violation.
 * Resolution notes are mandatory to ensure accountability.
 */
public class ViolationResolveDto {

    @NotBlank(message = "Resolution notes are required when resolving a violation")
    private String resolutionNotes;

    private BigDecimal fineAmount;

    public ViolationResolveDto() {
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
