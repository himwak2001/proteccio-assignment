package com.proteccio.dto;

import com.proteccio.model.Dataset;
import com.proteccio.model.SensitivityTag;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DatasetSummaryDto {
    public Long id;
    public String filename;
    public LocalDateTime uploadTime;
    public int rowCount;
    public int columnCount;
    public double qualityScore;
    public double trustScore;
    public long viewCount;
    public LocalDateTime lastViewedAt;
    public boolean lowActivity;
    public long sensitiveColumnCount;

    public static DatasetSummaryDto from(Dataset d) {
        DatasetSummaryDto dto = new DatasetSummaryDto();
        dto.id = d.getId();
        dto.filename = d.getFilename();
        dto.uploadTime = d.getUploadTime();
        dto.rowCount = d.getRowCount();
        dto.columnCount = d.getColumnCount();
        dto.qualityScore = d.getQualityScore();
        dto.trustScore = d.getTrustScore();
        dto.viewCount = d.getViewCount();
        dto.lastViewedAt = d.getLastViewedAt();
        dto.lowActivity = d.isLowActivity();
        dto.sensitiveColumnCount = d.getColumns().stream()
                .filter(c -> c.getSensitivityTag() != null && c.getSensitivityTag() != SensitivityTag.NONE)
                .count();
        return dto;
    }
}
