package com.wastewatch.dto.report;

import com.wastewatch.dto.violation.ViolationResponseDto;

import java.math.BigDecimal;
import java.util.List;

public class ViolationReportDto {

    private long totalViolations;
    private long openCount;
    private long acknowledgedCount;
    private long actionRequiredCount;
    private long resolvedCount;
    private long criticalCount;
    private long highCount;
    private long mediumCount;
    private long lowCount;
    private BigDecimal totalFinesImposed;
    private double resolutionRate;
    private List<ViolationResponseDto> violations;

    public ViolationReportDto() {
    }

    // Getters and Setters
    public long getTotalViolations() { return totalViolations; }
    public void setTotalViolations(long totalViolations) { this.totalViolations = totalViolations; }

    public long getOpenCount() { return openCount; }
    public void setOpenCount(long openCount) { this.openCount = openCount; }

    public long getAcknowledgedCount() { return acknowledgedCount; }
    public void setAcknowledgedCount(long acknowledgedCount) { this.acknowledgedCount = acknowledgedCount; }

    public long getActionRequiredCount() { return actionRequiredCount; }
    public void setActionRequiredCount(long actionRequiredCount) { this.actionRequiredCount = actionRequiredCount; }

    public long getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }

    public long getCriticalCount() { return criticalCount; }
    public void setCriticalCount(long criticalCount) { this.criticalCount = criticalCount; }

    public long getHighCount() { return highCount; }
    public void setHighCount(long highCount) { this.highCount = highCount; }

    public long getMediumCount() { return mediumCount; }
    public void setMediumCount(long mediumCount) { this.mediumCount = mediumCount; }

    public long getLowCount() { return lowCount; }
    public void setLowCount(long lowCount) { this.lowCount = lowCount; }

    public BigDecimal getTotalFinesImposed() { return totalFinesImposed; }
    public void setTotalFinesImposed(BigDecimal totalFinesImposed) { this.totalFinesImposed = totalFinesImposed; }

    public double getResolutionRate() { return resolutionRate; }
    public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }

    public List<ViolationResponseDto> getViolations() { return violations; }
    public void setViolations(List<ViolationResponseDto> violations) { this.violations = violations; }
}
