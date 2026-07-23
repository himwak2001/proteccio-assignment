package com.proteccio.controller;

import com.proteccio.dto.ColumnDetailDto;
import com.proteccio.dto.DatasetDetailDto;
import com.proteccio.dto.DatasetSummaryDto;
import com.proteccio.dto.TagOverrideRequest;
import com.proteccio.model.ColumnMeta;
import com.proteccio.model.Dataset;
import com.proteccio.model.SensitivityTag;
import com.proteccio.service.DatasetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {
    private final DatasetService datasetService;

    @PostMapping("/upload")
    public ResponseEntity<DatasetDetailDto> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Dataset dataset = datasetService.ingestAndCatalog(file);
        return ResponseEntity.ok(DatasetDetailDto.from(dataset));
    }

    @GetMapping
    public ResponseEntity<List<DatasetSummaryDto>> list() {
        List<DatasetSummaryDto> dtos = datasetService.listAll().stream().map(DatasetSummaryDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatasetDetailDto> getDetail(@PathVariable Long id) {
        Dataset dataset = datasetService.getDetailAndRecordView(id);
        return ResponseEntity.ok(DatasetDetailDto.from(dataset));
    }

    @PatchMapping("/{datasetId}/columns/{columnId}")
    public ResponseEntity<ColumnDetailDto> overrideTag(
            @PathVariable Long datasetId,
            @PathVariable Long columnId,
            @Valid @RequestBody TagOverrideRequest request) {

        SensitivityTag tag = SensitivityTag.valueOf(request.getSensitivityTag().toUpperCase());
        ColumnMeta updated = datasetService.overrideColumnTag(datasetId, columnId, tag);
        return ResponseEntity.ok(ColumnDetailDto.from(updated));
    }
}
