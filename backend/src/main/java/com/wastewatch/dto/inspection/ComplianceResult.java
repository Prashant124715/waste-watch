package com.wastewatch.dto.inspection;

import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.ViolationSeverity;

public class ComplianceResult {

    private int score;
    private ComplianceClassification status;
    private ScoreBreakdownDto breakdown;
    private boolean violationRequired;
    private ViolationSeverity violationSeverity;

    public ComplianceResult() {
    }

    public ComplianceResult(int score, ComplianceClassification status, ScoreBreakdownDto breakdown, boolean violationRequired, ViolationSeverity violationSeverity) {
        this.score = score;
        this.status = status;
        this.breakdown = breakdown;
        this.violationRequired = violationRequired;
        this.violationSeverity = violationSeverity;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public ComplianceClassification getStatus() {
        return status;
    }

    public void setStatus(ComplianceClassification status) {
        this.status = status;
    }

    public ScoreBreakdownDto getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(ScoreBreakdownDto breakdown) {
        this.breakdown = breakdown;
    }

    public boolean isViolationRequired() {
        return violationRequired;
    }

    public void setViolationRequired(boolean violationRequired) {
        this.violationRequired = violationRequired;
    }

    public ViolationSeverity getViolationSeverity() {
        return violationSeverity;
    }

    public void setViolationSeverity(ViolationSeverity violationSeverity) {
        this.violationSeverity = violationSeverity;
    }
}
