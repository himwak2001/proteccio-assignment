package com.proteccio.service;

import com.proteccio.model.DataType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TypeInferenceService {
    private static final double MAJORITY_THRESHOLD = 0.9;

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^-?\\d+\\.\\d+$");
    private static final Pattern BOOLEAN_PATTERN =
            Pattern.compile("^(true|false|yes|no|y|n|0|1)$", Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
    };

    public DataType inferType(List<String> columnValues) {
        List<String> nonBlank = columnValues.stream().filter(v -> v != null && !v.isBlank()).toList();
        if (nonBlank.isEmpty()) return DataType.EMPTY;

        int total = nonBlank.size();
        long intCount = nonBlank.stream().filter(v -> INTEGER_PATTERN.matcher(v).matches()).count();
        long decimalCount = nonBlank.stream()
                .filter(v -> DECIMAL_PATTERN.matcher(v).matches() || INTEGER_PATTERN.matcher(v).matches())
                .count();
        long boolCount = nonBlank.stream().filter(v -> BOOLEAN_PATTERN.matcher(v).matches()).count();
        long dateCount = nonBlank.stream().filter(this::looksLikeDate).count();

        if ((double) intCount / total >= MAJORITY_THRESHOLD) return DataType.INTEGER;
        if ((double) decimalCount / total >= MAJORITY_THRESHOLD) return DataType.DECIMAL;
        if ((double) boolCount / total >= MAJORITY_THRESHOLD) return DataType.BOOLEAN;
        if ((double) dateCount / total >= MAJORITY_THRESHOLD) return DataType.DATE;
        return DataType.STRING;
    }

    // used later by the quality checker to flag values that don't fit the type we picked
    public boolean matchesType(String value, DataType type) {
        if (value == null || value.isBlank()) return true; // blanks are handled separately as "missing"
        return switch (type) {
            case INTEGER -> INTEGER_PATTERN.matcher(value).matches();
            case DECIMAL -> DECIMAL_PATTERN.matcher(value).matches() || INTEGER_PATTERN.matcher(value).matches();
            case BOOLEAN -> BOOLEAN_PATTERN.matcher(value).matches();
            case DATE -> looksLikeDate(value);
            case STRING, EMPTY -> true;
        };
    }

    private boolean looksLikeDate(String value) {
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                LocalDate.parse(value, fmt);
                return true;
            } catch (Exception ignored) {
                // not this format, try the next one
            }
        }
        return false;
    }
}
