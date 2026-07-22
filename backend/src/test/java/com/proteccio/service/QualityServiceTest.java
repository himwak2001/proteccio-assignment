package com.proteccio.service;

import com.proteccio.model.DataType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QualityServiceTest {

    private final QualityService qualityService = new QualityService();
    private final TypeInferenceService typeInferenceService = new TypeInferenceService();

    @Test
    void missingPercentageIsCalculatedCorrectly() {
        List<String> values = Arrays.asList("a", "", null, "b"); // 2 of 4 missing
        double missingPct = qualityService.evaluateMissingPct(values);
        assertEquals(50.0, missingPct, 0.001);
    }

    @Test
    void invalidValuesAreCountedAgainstInferredType() {
        List<String> values = Arrays.asList("10", "20", "not-a-number", "30");
        int invalidCount = qualityService.evaluateInvalidCount(values, DataType.INTEGER, typeInferenceService);
        assertEquals(1, invalidCount);
    }

    @Test
    void duplicateRowsAreCountedOnExactMatch() {
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("1", "Alice"),
                Arrays.asList("2", "Bob"),
                Arrays.asList("1", "Alice"), // duplicate
                Arrays.asList("3", "Carol")
        );
        assertEquals(1, qualityService.countDuplicateRows(rows));
    }

    @Test
    void cleanDataGetsAPerfectScore() {
        double score = qualityService.computeQualityScore(0, 0, 100, 0, 10);
        assertEquals(100.0, score, 0.001);
    }

    @Test
    void scoreNeverGoesBelowZero() {
        double score = qualityService.computeQualityScore(100, 1000, 100, 100, 10);
        assertTrue(score >= 0.0);
    }
}