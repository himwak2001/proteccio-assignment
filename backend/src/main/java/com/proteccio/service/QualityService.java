package com.proteccio.service;

import com.proteccio.model.DataType;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QualityService {
    public double evaluateMissingPct(List<String> values) {
        if (values.isEmpty()) return 0;
        long missing = values.stream().filter(v -> v == null || v.isBlank()).count();
        return (double) missing / values.size() * 100.0;
    }

    public int evaluateInvalidCount(List<String> values, DataType type, TypeInferenceService typeInferenceService) {
        int invalid = 0;
        for (String v : values) {
            if (v != null && !v.isBlank() && !typeInferenceService.matchesType(v, type)) {
                invalid++;
            }
        }
        return invalid;
    }

    public int countDuplicateRows(List<List<String>> rows) {
        Set<String> seen = new HashSet<>();
        int duplicateCount = 0;
        for (List<String> row : rows) {
            String key = String.join("\u0001", row);
            if (!seen.add(key)) {
                duplicateCount++;
            }
        }
        return duplicateCount;
    }

    public double computeQualityScore(double avgMissingPct, int invalidValueCount, int totalCellCount,
                                      int duplicateRowCount, int totalRowCount) {
        double invalidRate = totalCellCount == 0 ? 0 : (double) invalidValueCount / totalCellCount * 100.0;
        double duplicateRate = totalRowCount == 0 ? 0 : (double) duplicateRowCount / totalRowCount * 100.0;

        double score = 100.0
                - (avgMissingPct * 0.5)
                - (invalidRate * 0.3)
                - (duplicateRate * 0.2);

        return Math.max(0.0, Math.min(100.0, score));
    }
}
