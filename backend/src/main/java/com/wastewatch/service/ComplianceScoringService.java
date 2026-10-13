package com.wastewatch.service;

import com.wastewatch.dto.inspection.ComplianceResult;
import com.wastewatch.dto.inspection.InspectionRequestDto;
import com.wastewatch.dto.inspection.ScoreBreakdownDto;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.ViolationSeverity;
import org.springframework.stereotype.Service;

/**
 * Authoritative 100-point compliance scoring engine for WasteWatch waste segregation audits.
 *
 * Scoring Weights:
 * - Wet Waste Separated: 25 pts
 * - Dry Waste Separated: 25 pts
 * - Sanitary / Reject Waste Separated: 20 pts
 * - Correct Bins Used: 15 pts
 * - Bins Clearly Labelled: 10 pts
 * - No Mixed Waste Contamination: 5 pts
 *
 * Classification Tiers:
 * - 80 - 100 pts : EXCELLENT
 * - 60 - 79 pts  : GOOD
 * - 40 - 59 pts  : NEEDS_IMPROVEMENT
 * -  0 - 39 pts  : POOR (Triggers automated violation ticket)
 *
 * Edge Cases & Business Rules:
 * 1. Null Safety: Any null or missing checklist boolean is safely treated as false (0 points).
 * 2. Mixed Contamination Invariant: If zero waste categories are separated (wet, dry, and sanitary all false/null),
 *    no mixed waste points cannot be awarded even if requested, as all waste is inherently co-mingled.
 */
@Service
public class ComplianceScoringService {

    public static final int WET_WASTE_WEIGHT = 25;
    public static final int DRY_WASTE_WEIGHT = 25;
    public static final int SANITARY_WASTE_WEIGHT = 20;
    public static final int CORRECT_BINS_WEIGHT = 15;
    public static final int BINS_LABELLED_WEIGHT = 10;
    public static final int NO_MIXED_WASTE_WEIGHT = 5;

    public static final int EXCELLENT_THRESHOLD = 80;
    public static final int GOOD_THRESHOLD = 60;
    public static final int NEEDS_IMPROVEMENT_THRESHOLD = 40;

    /**
     * Backward-compatible inner result class for existing services
     */
    public static class ScoringResult {
        private final int totalScore;
        private final ComplianceClassification classification;
        private final boolean violationRequired;
        private final ViolationSeverity violationSeverity;
        private final ScoreBreakdownDto breakdown;

        public ScoringResult(int totalScore, ComplianceClassification classification, boolean violationRequired, ViolationSeverity violationSeverity, ScoreBreakdownDto breakdown) {
            this.totalScore = totalScore;
            this.classification = classification;
            this.violationRequired = violationRequired;
            this.violationSeverity = violationSeverity;
            this.breakdown = breakdown;
        }

        public int getTotalScore() {
            return totalScore;
        }

        public ComplianceClassification getClassification() {
            return classification;
        }

        public boolean isViolationRequired() {
            return violationRequired;
        }

        public ViolationSeverity getViolationSeverity() {
            return violationSeverity;
        }

        public ScoreBreakdownDto getBreakdown() {
            return breakdown;
        }
    }

    /**
     * Calculates the full ComplianceResult from an incoming InspectionRequestDto.
     */
    public ComplianceResult calculateCompliance(InspectionRequestDto dto) {
        if (dto == null) {
            return calculateCompliance(null, null, null, null, null, null);
        }
        return calculateCompliance(
                dto.getWetWasteSeparated(),
                dto.getDryWasteSeparated(),
                dto.getSanitaryWasteSeparated(),
                dto.getCorrectBinsUsed(),
                dto.getBinsLabelled(),
                dto.getNoMixedWaste()
        );
    }

    /**
     * Primary evaluation engine calculating total score, status classification, and individual point breakdown.
     */
    public ComplianceResult calculateCompliance(Boolean wet, Boolean dry, Boolean sanitary,
                                               Boolean correctBins, Boolean binsLabelled, Boolean noMixed) {
        int wetPoints = Boolean.TRUE.equals(wet) ? WET_WASTE_WEIGHT : 0;
        int dryPoints = Boolean.TRUE.equals(dry) ? DRY_WASTE_WEIGHT : 0;
        int sanitaryPoints = Boolean.TRUE.equals(sanitary) ? SANITARY_WASTE_WEIGHT : 0;
        int correctBinsPoints = Boolean.TRUE.equals(correctBins) ? CORRECT_BINS_WEIGHT : 0;
        int labelledBinsPoints = Boolean.TRUE.equals(binsLabelled) ? BINS_LABELLED_WEIGHT : 0;

        // Invariant: noMixedWaste bonus (+5) is awarded only when noMixed is true AND
        // at least one segregation stream is active (otherwise all unsegregated waste is co-mingled).
        boolean hasAnySegregation = Boolean.TRUE.equals(wet) || Boolean.TRUE.equals(dry) || Boolean.TRUE.equals(sanitary);
        int noMixedPoints = (Boolean.TRUE.equals(noMixed) && (hasAnySegregation || (!Boolean.TRUE.equals(wet) && !Boolean.TRUE.equals(dry) && !Boolean.TRUE.equals(sanitary) && Boolean.FALSE.equals(noMixed))))
                ? (Boolean.TRUE.equals(noMixed) && hasAnySegregation ? NO_MIXED_WASTE_WEIGHT : 0)
                : (Boolean.TRUE.equals(noMixed) && hasAnySegregation ? NO_MIXED_WASTE_WEIGHT : 0);

        // Standardized calculation
        if (Boolean.TRUE.equals(noMixed) && hasAnySegregation) {
            noMixedPoints = NO_MIXED_WASTE_WEIGHT;
        } else {
            noMixedPoints = 0;
        }

        int totalScore = wetPoints + dryPoints + sanitaryPoints + correctBinsPoints + labelledBinsPoints + noMixedPoints;

        ComplianceClassification status = classifyScore(totalScore);
        boolean violationRequired = (totalScore < NEEDS_IMPROVEMENT_THRESHOLD);
        ViolationSeverity severity = determineSeverity(totalScore);

        ScoreBreakdownDto breakdown = new ScoreBreakdownDto(
                wetPoints,
                dryPoints,
                sanitaryPoints,
                correctBinsPoints,
                labelledBinsPoints,
                noMixedPoints
        );

        return new ComplianceResult(totalScore, status, breakdown, violationRequired, severity);
    }

    /**
     * Adapter method for existing InspectionService.
     */
    public ScoringResult calculateScore(InspectionRequestDto dto) {
        ComplianceResult result = calculateCompliance(dto);
        return new ScoringResult(
                result.getScore(),
                result.getStatus(),
                result.isViolationRequired(),
                result.getViolationSeverity(),
                result.getBreakdown()
        );
    }

    /**
     * Maps an integer score to the authoritative ComplianceClassification tier.
     */
    public ComplianceClassification classifyScore(int score) {
        if (score >= EXCELLENT_THRESHOLD) {
            return ComplianceClassification.EXCELLENT;
        } else if (score >= GOOD_THRESHOLD) {
            return ComplianceClassification.GOOD;
        } else if (score >= NEEDS_IMPROVEMENT_THRESHOLD) {
            return ComplianceClassification.NEEDS_IMPROVEMENT;
        } else {
            return ComplianceClassification.POOR;
        }
    }

    /**
     * Determines the administrative violation severity tier for a given score.
     */
    public ViolationSeverity determineSeverity(int score) {
        if (score < 20) {
            return ViolationSeverity.CRITICAL;
        } else if (score < NEEDS_IMPROVEMENT_THRESHOLD) {
            return ViolationSeverity.HIGH;
        } else if (score < GOOD_THRESHOLD) {
            return ViolationSeverity.MEDIUM;
        } else {
            return ViolationSeverity.LOW;
        }
    }
}
