package com.proteccio.service;

import com.proteccio.model.DataType;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QualityService {
    /**
     * % of blank/null values in a column.
     *
     * @param values every value in the column, including blanks
     * @return missing percentage, 0-100
     */
    public double evaluateMissingPct(List<String> values) {
        if (values.isEmpty()) return 0;
        long missing = values.stream().filter(v -> v == null || v.isBlank()).count();
        return (double) missing / values.size() * 100.0;
    }

    /**
     * Counts values that don't match the type we inferred for this column.
     * Blanks aren't counted here - they're already covered by the missing % check.
     *
     * @param values               every value in the column
     * @param type                 the type inferred for this column
     * @param typeInferenceService used to check each value against that type
     * @return count of values that look wrong for the column's type
     */
    public int evaluateInvalidCount(List<String> values, DataType type, TypeInferenceService typeInferenceService) {
        int invalid = 0;
        for (String v : values) {
            if (v != null && !v.isBlank() && !typeInferenceService.matchesType(v, type)) {
                invalid++;
            }
        }
        return invalid;
    }

    /**
     * Counts exact-duplicate rows in the dataset (every column matches).
     * Doesn't try to catch near-duplicates or fuzzy matches - keeping this simple.
     *
     * @param rows all rows in the dataset
     * @return number of rows that are duplicates of an earlier row
     */
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

    /**
     * Rolls missing %, invalid values, and duplicate rows into one 0-100 score.
     * Missing data is weighted heaviest (0.5) since it affects every check
     * downstream. Invalid values (0.3) and duplicates (0.2) matter less because
     * some amount of both is normal in real files.
     *
     * @param avgMissingPct     average missing % across all columns
     * @param invalidValueCount total invalid values across the whole dataset
     * @param totalCellCount    rows x columns, used to turn invalid count into a rate
     * @param duplicateRowCount how many duplicate rows were found
     * @param totalRowCount     total rows, used to turn duplicate count into a rate
     * @return quality score, clamped between 0 and 100
     */
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
