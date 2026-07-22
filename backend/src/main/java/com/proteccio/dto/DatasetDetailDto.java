package com.proteccio.dto;

import com.proteccio.model.Dataset;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DatasetDetailDto {
    public Long id;
    public String filename;
    public LocalDateTime uploadTime;
    public int rowCount;
    public int columnCount;
    public double qualityScore;
    public double missingValuePct;
    public int duplicateRowCount;
    public int invalidValueCount;
    public double trustScore;
    public long viewCount;
    public LocalDateTime lastViewedAt;
    public boolean lowActivity;
    public List<ColumnDetailDto> columns;

    public static DatasetDetailDto from(Dataset d) {
        DatasetDetailDto dto = new DatasetDetailDto();
        dto.id = d.getId();
        dto.filename = d.getFilename();
        dto.uploadTime = d.getUploadTime();
        dto.rowCount = d.getRowCount();
        dto.columnCount = d.getColumnCount();
        dto.qualityScore = d.getQualityScore();
        dto.missingValuePct = d.getMissingValuePct();
        dto.duplicateRowCount = d.getDuplicateRowCount();
        dto.invalidValueCount = d.getInvalidValueCount();
        dto.trustScore = d.getTrustScore();
        dto.viewCount = d.getViewCount();
        dto.lastViewedAt = d.getLastViewedAt();
        dto.lowActivity = d.isLowActivity();
        dto.columns = d.getColumns().stream()
                .sorted((a, b) -> Integer.compare(a.getOrdinalPosition(), b.getOrdinalPosition()))
                .map(ColumnDetailDto::from)
                .collect(Collectors.toList());
        return dto;
    }
}
