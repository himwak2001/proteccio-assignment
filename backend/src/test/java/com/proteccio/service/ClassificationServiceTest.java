package com.proteccio.service;

import com.proteccio.model.SensitivityTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassificationServiceTest {

    private final ClassificationService classificationService = new ClassificationService();

    @Test
    void tagsColumnByHeaderName() {
        SensitivityTag tag = classificationService.classify("email_address", List.of("a@b.com", "c@d.com"));
        assertEquals(SensitivityTag.EMAIL, tag);
    }

    @Test
    void fallsBackToValuePatternWhenHeaderIsGeneric() {
        SensitivityTag tag = classificationService.classify(
                "contact", List.of("john@company.com", "jane@company.com", "bob@company.com"));
        assertEquals(SensitivityTag.EMAIL, tag);
    }

    @Test
    void ordinaryNumericColumnIsNotFlagged() {
        SensitivityTag tag = classificationService.classify("quantity", List.of("10", "20", "5", "8"));
        assertEquals(SensitivityTag.NONE, tag);
    }

    @Test
    void surrogateKeyIdColumnIsNotFlaggedAsSensitive() {
        SensitivityTag tag = classificationService.classify("id", List.of("1", "2", "3", "4"));
        assertEquals(SensitivityTag.NONE, tag);
    }

    @Test
    void nameColumnIsFlaggedByHeader() {
        SensitivityTag tag = classificationService.classify("full_name", List.of("John Smith", "Jane Doe"));
        assertEquals(SensitivityTag.PERSON_NAME, tag);
    }
}