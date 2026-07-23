package com.proteccio.service;

import com.proteccio.exception.InvalidDatasetException;
import com.proteccio.model.ColumnMeta;
import com.proteccio.model.DataType;
import com.proteccio.model.Dataset;
import com.proteccio.model.SensitivityTag;
import com.proteccio.repository.DatasetRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DatasetService {
    private static final int CLASSIFICATION_SAMPLE_SIZE = 50;
    private static final int DISTINCT_COUNT_CAP = 1000;

    private final DatasetRepository datasetRepository;
    private final IngestionService ingestionService;
    private final TypeInferenceService typeInferenceService;
    private final ClassificationService classificationService;
    private final QualityService qualityService;
    private final TrustService trustService;
    private final ValueService valueService;

    public DatasetService(DatasetRepository datasetRepository,
                          IngestionService ingestionService,
                          TypeInferenceService typeInferenceService,
                          ClassificationService classificationService,
                          QualityService qualityService,
                          TrustService trustService,
                          ValueService valueService) {
        this.datasetRepository = datasetRepository;
        this.ingestionService = ingestionService;
        this.typeInferenceService = typeInferenceService;
        this.classificationService = classificationService;
        this.qualityService = qualityService;
        this.trustService = trustService;
        this.valueService = valueService;
    }

    /**
     * Runs the full pipeline on a newly uploaded file: parse it, figure out each
     * column's type, classify anything sensitive, run the quality checks, and
     * work out trust + activity status. Saves the finished Dataset at the end.
     *
     * @param file the uploaded CSV/XLSX
     * @return the saved dataset with all columns and scores filled in
     * @throws IOException if the file can't be parsed
     */
    @Transactional
    public Dataset ingestAndCatalog(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new InvalidDatasetException("Uploaded file is empty");
        }

        ParsedDataset parsed = ingestionService.parse(file);
        if (parsed.getHeaders().isEmpty()) {
            throw new InvalidDatasetException("Could not detect any columns in the uploaded file");
        }

        List<String> headers = parsed.getHeaders();
        List<List<String>> rows = parsed.getRows();

        Dataset dataset = new Dataset();
        dataset.setFilename(file.getOriginalFilename());
        dataset.setUploadTime(LocalDateTime.now());
        dataset.setRowCount(rows.size());
        dataset.setColumnCount(headers.size());

        double totalMissingPct = 0;
        int totalInvalid = 0;
        int totalCells = rows.size() * headers.size();

        for (int col = 0; col < headers.size(); col++) {
            List<String> columnValues = extractColumn(rows, col);

            DataType type = typeInferenceService.inferType(columnValues);
            double missingPct = qualityService.evaluateMissingPct(columnValues);
            int invalidCount = qualityService.evaluateInvalidCount(columnValues, type, typeInferenceService);

            List<String> sample = columnValues.stream()
                    .filter(v -> v != null && !v.isBlank())
                    .limit(CLASSIFICATION_SAMPLE_SIZE)
                    .toList();
            SensitivityTag tag = classificationService.classify(headers.get(col), sample);

            ColumnMeta columnMeta = new ColumnMeta();
            columnMeta.setDataset(dataset);
            columnMeta.setColumnName(headers.get(col));
            columnMeta.setOrdinalPosition(col);
            columnMeta.setInferredType(type);
            columnMeta.setSensitivityTag(tag);
            columnMeta.setMissingPct(missingPct);
            columnMeta.setInvalidCount(invalidCount);
            columnMeta.setDistinctCount(Math.min(
                    (int) columnValues.stream().filter(v -> v != null && !v.isBlank()).distinct().count(),
                    DISTINCT_COUNT_CAP));

            dataset.getColumns().add(columnMeta);

            totalMissingPct += missingPct;
            totalInvalid += invalidCount;
        }

        double avgMissingPct = headers.isEmpty() ? 0 : totalMissingPct / headers.size();
        int duplicateRows = qualityService.countDuplicateRows(rows);
        double qualityScore = qualityService.computeQualityScore(avgMissingPct, totalInvalid, totalCells, duplicateRows, rows.size());

        dataset.setMissingValuePct(avgMissingPct);
        dataset.setInvalidValueCount(totalInvalid);
        dataset.setDuplicateRowCount(duplicateRows);
        dataset.setQualityScore(round2(qualityScore));

        double trustScore = trustService.computeTrustScore(qualityScore, dataset.getColumns());
        dataset.setTrustScore(round2(trustScore));

        valueService.refreshActivityFlag(dataset);

        return datasetRepository.save(dataset);
    }

    /**
     * @return every dataset in the catalog, for the dashboard list view
     */
    public List<Dataset> listAll() {
        return datasetRepository.findAll();
    }

    /**
     * Fetches a dataset for the detail page and counts it as a view.
     *
     * @param id dataset id
     * @return the dataset with its columns
     */
    @Transactional
    public Dataset getDetailAndRecordView(Long id) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dataset " + id + " not found"));
        valueService.recordView(dataset);
        return datasetRepository.save(dataset);
    }

    /**
     * Lets a user manually correct a column's sensitivity tag. Also recalculates
     * the trust score since classification completeness just changed.
     *
     * @param datasetId which dataset the column belongs to
     * @param columnId which column to update
     * @param newTag the tag the user picked
     * @return the updated column
     */
    @Transactional
    public ColumnMeta overrideColumnTag(Long datasetId, Long columnId, SensitivityTag newTag) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new NoSuchElementException("Dataset " + datasetId + " not found"));

        ColumnMeta column = dataset.getColumns().stream()
                .filter(c -> c.getId().equals(columnId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Column " + columnId + " not found"));

        column.setSensitivityTag(newTag);
        column.setTagOverridden(true);

        // classification just changed, so trust score needs to be recalculated too
        double trustScore = trustService.computeTrustScore(dataset.getQualityScore(), dataset.getColumns());
        dataset.setTrustScore(round2(trustScore));

        datasetRepository.save(dataset);
        return column;
    }

    private List<String> extractColumn(List<List<String>> rows, int colIndex) {
        return rows.stream().map(row -> colIndex < row.size() ? row.get(colIndex) : "").toList();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
