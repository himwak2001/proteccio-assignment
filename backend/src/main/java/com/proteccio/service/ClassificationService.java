package com.proteccio.service;

import com.proteccio.model.SensitivityTag;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ClassificationService {
    private static final Pattern EMAIL_VALUE = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_VALUE = Pattern.compile("^\\+?[0-9()\\-\\s]{7,15}$");
    private static final Pattern ID_VALUE = Pattern.compile("^[A-Za-z]{0,4}\\d{4,}$");

    private static final double SAMPLE_MATCH_THRESHOLD = 0.6;

    public SensitivityTag classify(String columnName, List<String> sampleValues) {
        String header = columnName == null ? "" : columnName.toLowerCase().trim();

        if (containsAny(header, "email", "e-mail")) return SensitivityTag.EMAIL;
        if (containsAny(header, "phone", "mobile", "contact_no", "contact number", "tel"))
            return SensitivityTag.PHONE_NUMBER;
        if (containsAny(header, "first_name", "last_name", "firstname", "lastname", "full_name", "fullname")
                || header.equals("name")) return SensitivityTag.PERSON_NAME;
        if (containsAny(header, "ssn", "passport", "aadhaar", "aadhar", "pan_number", "national_id"))
            return SensitivityTag.ID_NUMBER;
        if (containsAny(header, "address", "street", "zipcode", "zip_code", "postal")) return SensitivityTag.ADDRESS;
        if (containsAny(header, "dob", "date_of_birth", "birthdate", "birth_date")) return SensitivityTag.DATE_OF_BIRTH;
        if (containsAny(header, "salary", "account_number", "iban", "card_number", "credit_card", "bank"))
            return SensitivityTag.FINANCIAL;

        if (header.endsWith("_id") || header.equals("id")) {
            // don't flag every surrogate primary key as an ID number - only if
            // the actual values look like a real identifier (aadhaar, passport etc)
            if (matchesSample(sampleValues, ID_VALUE)) return SensitivityTag.ID_NUMBER;
        }

        // header gave no hint - check if the values themselves look sensitive
        if (matchesSample(sampleValues, EMAIL_VALUE)) return SensitivityTag.EMAIL;
        if (matchesSample(sampleValues, PHONE_VALUE) && looksNumericPhone(sampleValues))
            return SensitivityTag.PHONE_NUMBER;

        return SensitivityTag.NONE;
    }

    private boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) return true;
        }
        return false;
    }

    private boolean matchesSample(List<String> sampleValues, Pattern pattern) {
        List<String> nonBlank = sampleValues.stream().filter(v -> v != null && !v.isBlank()).toList();
        if (nonBlank.isEmpty()) return false;
        long matches = nonBlank.stream().filter(v -> pattern.matcher(v.trim()).matches()).count();
        return (double) matches / nonBlank.size() >= SAMPLE_MATCH_THRESHOLD;
    }

    // the loose phone regex can also match short numeric ids, so double check digit count
    private boolean looksNumericPhone(List<String> sampleValues) {
        return sampleValues.stream()
                .filter(v -> v != null && !v.isBlank())
                .anyMatch(v -> v.replaceAll("[^0-9]", "").length() >= 7);
    }
}
