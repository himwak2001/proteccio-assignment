package com.proteccio.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IngestionService {
    public ParsedDataset parse(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();

        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return parseExcel(file);
        }
        return parseCsv(file);
    }

    private ParsedDataset parseCsv(MultipartFile file) throws IOException {
        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .setAllowMissingColumnNames(true)
                     .build()
                     .parse(reader)) {

            List<String> headers = cleanHeaders(new ArrayList<>(parser.getHeaderNames()));

            List<List<String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < headers.size(); i++) {
                    String value = i < record.size() ? record.get(i) : "";
                    row.add(value == null ? "" : value.trim());
                }
                if (row.stream().anyMatch(v -> !v.isBlank())) {
                    rows.add(row);
                }
            }
            return new ParsedDataset(headers, rows);
        }
    }

    private ParsedDataset parseExcel(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            var rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return new ParsedDataset(new ArrayList<>(), new ArrayList<>());
            }

            Row headerRow = rowIterator.next();
            List<String> headers = new ArrayList<>();
            int lastCol = headerRow.getLastCellNum();
            for (int i = 0; i < lastCol; i++) {
                Cell cell = headerRow.getCell(i);
                headers.add(cell == null ? "" : formatter.formatCellValue(cell).trim());
            }
            headers = cleanHeaders(headers);

            List<List<String>> rows = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row excelRow = rowIterator.next();
                List<String> row = new ArrayList<>();
                boolean allBlank = true;
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = excelRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String value = cell == null ? "" : formatter.formatCellValue(cell).trim();
                    if (!value.isBlank()) allBlank = false;
                    row.add(value);
                }
                if (!allBlank) rows.add(row);
            }
            return new ParsedDataset(headers, rows);
        }
    }

    // blank headers get a fallback name, duplicate headers get a numeric suffix
    private List<String> cleanHeaders(List<String> rawHeaders) {
        List<String> result = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        for (int i = 0; i < rawHeaders.size(); i++) {
            String h = rawHeaders.get(i) == null ? "" : rawHeaders.get(i).trim();
            if (h.isBlank()) h = "column_" + (i + 1);
            int count = seen.merge(h, 1, Integer::sum);
            result.add(count == 1 ? h : h + "_" + count);
        }
        return result;
    }
}
