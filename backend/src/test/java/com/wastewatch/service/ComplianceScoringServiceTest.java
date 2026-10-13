package com.wastewatch.service;

import com.wastewatch.dto.inspection.ComplianceResult;
import com.wastewatch.dto.inspection.InspectionRequestDto;
import com.wastewatch.dto.inspection.ScoreBreakdownDto;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.ViolationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ComplianceScoringServiceTest {

    private ComplianceScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ComplianceScoringService();
    }

    @Test
    @DisplayName("Score 100: All conditions met yields 100 score, EXCELLENT classification, and no violation")
    void testScore100AllConditionsMet() {
        ComplianceResult result = scoringService.calculateCompliance(true, true, true, true, true, true);

        assertEquals(100, result.getScore());
        assertEquals(ComplianceClassification.EXCELLENT, result.getStatus());
        assertFalse(result.isViolationRequired());
        assertEquals(ViolationSeverity.LOW, result.getViolationSeverity());

        ScoreBreakdownDto b = result.getBreakdown();
        assertEquals(25, b.getWetWaste());
        assertEquals(25, b.getDryWaste());
        assertEquals(20, b.getSanitaryWaste());
        assertEquals(15, b.getCorrectBins());
        assertEquals(10, b.getLabelledBins());
        assertEquals(5, b.getNoMixedWaste());
    }

    @Test
    @DisplayName("Score 90: Missing labelled bins (100 - 10 = 90) yields EXCELLENT classification")
    void testScore90MissingLabelledBins() {
        // 25 + 25 + 20 + 15 + 0 + 5 = 90
        ComplianceResult result = scoringService.calculateCompliance(true, true, true, true, false, true);

        assertEquals(90, result.getScore());
        assertEquals(ComplianceClassification.EXCELLENT, result.getStatus());
        assertFalse(result.isViolationRequired());
        assertEquals(ViolationSeverity.LOW, result.getViolationSeverity());
        assertEquals(0, result.getBreakdown().getLabelledBins());
    }

    @Test
    @DisplayName("Score 80: Lower threshold for EXCELLENT classification")
    void testScore80ExcellentThreshold() {
        // 25 + 25 + 20 + 0 + 10 + 0 = 80
        ComplianceResult result = scoringService.calculateCompliance(true, true, true, false, true, false);

        assertEquals(80, result.getScore());
        assertEquals(ComplianceClassification.EXCELLENT, result.getStatus());
        assertFalse(result.isViolationRequired());
        assertEquals(ViolationSeverity.LOW, result.getViolationSeverity());
    }

    @Test
    @DisplayName("Score 79: Upper threshold for GOOD classification")
    void testScore79GoodUpperThreshold() {
        assertEquals(ComplianceClassification.GOOD, scoringService.classifyScore(79));
        assertEquals(ViolationSeverity.LOW, scoringService.determineSeverity(79));
    }

    @Test
    @DisplayName("Score 60: Lower threshold for GOOD classification")
    void testScore60GoodLowerThreshold() {
        // 25 + 25 + 0 + 0 + 10 + 0 = 60
        ComplianceResult result = scoringService.calculateCompliance(true, true, false, false, true, false);

        assertEquals(60, result.getScore());
        assertEquals(ComplianceClassification.GOOD, result.getStatus());
        assertFalse(result.isViolationRequired());
        assertEquals(ViolationSeverity.LOW, result.getViolationSeverity());
    }

    @Test
    @DisplayName("Score 59: Upper threshold for NEEDS_IMPROVEMENT classification")
    void testScore59NeedsImprovementUpperThreshold() {
        assertEquals(ComplianceClassification.NEEDS_IMPROVEMENT, scoringService.classifyScore(59));
        assertFalse(scoringService.classifyScore(59) == ComplianceClassification.GOOD);
        assertEquals(ViolationSeverity.MEDIUM, scoringService.determineSeverity(59));
    }

    @Test
    @DisplayName("Score 40: Lower threshold for NEEDS_IMPROVEMENT (non-violating boundary)")
    void testScore40NeedsImprovementLowerThreshold() {
        // 25 + 0 + 0 + 15 + 0 + 0 = 40
        ComplianceResult result = scoringService.calculateCompliance(true, false, false, true, false, false);

        assertEquals(40, result.getScore());
        assertEquals(ComplianceClassification.NEEDS_IMPROVEMENT, result.getStatus());
        assertFalse(result.isViolationRequired(), "Score 40 does not trigger an automatic violation");
        assertEquals(ViolationSeverity.MEDIUM, result.getViolationSeverity());
    }

    @Test
    @DisplayName("Score 39: Upper threshold for POOR classification (triggers auto-violation)")
    void testScore39PoorUpperThreshold() {
        assertEquals(ComplianceClassification.POOR, scoringService.classifyScore(39));
        assertEquals(ViolationSeverity.HIGH, scoringService.determineSeverity(39));
    }

    @Test
    @DisplayName("Score 0: All criteria false yields 0 score, POOR classification, and CRITICAL violation")
    void testScore0AllCriteriaFalse() {
        ComplianceResult result = scoringService.calculateCompliance(false, false, false, false, false, false);

        assertEquals(0, result.getScore());
        assertEquals(ComplianceClassification.POOR, result.getStatus());
        assertTrue(result.isViolationRequired());
        assertEquals(ViolationSeverity.CRITICAL, result.getViolationSeverity());

        ScoreBreakdownDto b = result.getBreakdown();
        assertEquals(0, b.getWetWaste());
        assertEquals(0, b.getDryWaste());
        assertEquals(0, b.getSanitaryWaste());
        assertEquals(0, b.getCorrectBins());
        assertEquals(0, b.getLabelledBins());
        assertEquals(0, b.getNoMixedWaste());
    }

    @Test
    @DisplayName("Individual Criteria Points: Verifies accurate weights for each individual criterion")
    void testIndividualCriteriaWeights() {
        // Wet waste alone: 25
        ComplianceResult wetOnly = scoringService.calculateCompliance(true, false, false, false, false, false);
        assertEquals(25, wetOnly.getScore());
        assertEquals(25, wetOnly.getBreakdown().getWetWaste());

        // Dry waste alone: 25
        ComplianceResult dryOnly = scoringService.calculateCompliance(false, true, false, false, false, false);
        assertEquals(25, dryOnly.getScore());
        assertEquals(25, dryOnly.getBreakdown().getDryWaste());

        // Sanitary waste alone: 20
        ComplianceResult sanitaryOnly = scoringService.calculateCompliance(false, false, true, false, false, false);
        assertEquals(20, sanitaryOnly.getScore());
        assertEquals(20, sanitaryOnly.getBreakdown().getSanitaryWaste());

        // Correct bins alone: 15
        ComplianceResult binsOnly = scoringService.calculateCompliance(false, false, false, true, false, false);
        assertEquals(15, binsOnly.getScore());
        assertEquals(15, binsOnly.getBreakdown().getCorrectBins());

        // Labelled bins alone: 10
        ComplianceResult labelledOnly = scoringService.calculateCompliance(false, false, false, false, true, false);
        assertEquals(10, labelledOnly.getScore());
        assertEquals(10, labelledOnly.getBreakdown().getLabelledBins());

        // No mixed waste with active segregation: +5
        ComplianceResult wetAndNoMixed = scoringService.calculateCompliance(true, false, false, false, false, true);
        assertEquals(30, wetAndNoMixed.getScore());
        assertEquals(5, wetAndNoMixed.getBreakdown().getNoMixedWaste());
    }

    @Test
    @DisplayName("Edge Case: Missing/Null values are safely handled without throwing NullPointerException")
    void testNullSafety() {
        ComplianceResult result = scoringService.calculateCompliance(null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getScore());
        assertEquals(ComplianceClassification.POOR, result.getStatus());
        assertTrue(result.isViolationRequired());
        assertEquals(ViolationSeverity.CRITICAL, result.getViolationSeverity());

        // Null DTO
        ComplianceResult nullDtoResult = scoringService.calculateCompliance((InspectionRequestDto) null);
        assertNotNull(nullDtoResult);
        assertEquals(0, nullDtoResult.getScore());
    }

    @Test
    @DisplayName("Edge Case: Invariant rule prevents awarding 'noMixedWaste' bonus when all segregation streams fail")
    void testMixedContaminationInvariantRule() {
        // Even if noMixedWaste is checked true, if no streams are segregated, waste is mixed by definition
        ComplianceResult result = scoringService.calculateCompliance(false, false, false, false, false, true);

        assertEquals(0, result.getScore(), "No mixed waste bonus cannot be claimed without segregation");
        assertEquals(0, result.getBreakdown().getNoMixedWaste());
    }

    @ParameterizedTest(name = "Score {0} maps to Classification {1}")
    @CsvSource({
            "100, EXCELLENT",
            "85,  EXCELLENT",
            "80,  EXCELLENT",
            "79,  GOOD",
            "70,  GOOD",
            "60,  GOOD",
            "59,  NEEDS_IMPROVEMENT",
            "50,  NEEDS_IMPROVEMENT",
            "40,  NEEDS_IMPROVEMENT",
            "39,  POOR",
            "20,  POOR",
            "15,  POOR",
            "0,   POOR"
    })
    void testClassificationBoundaries(int score, ComplianceClassification expectedClassification) {
        assertEquals(expectedClassification, scoringService.classifyScore(score));
    }
}
