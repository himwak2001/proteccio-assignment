package com.proteccio.dto;

import com.proteccio.model.ColumnMeta;
import lombok.Data;

@Data
public class ColumnDetailDto {
    public Long id;
    public String columnName;
    public String inferredType;
    public String sensitivityTag;
    public boolean tagOverridden;
    public double missingPct;
    public int invalidCount;
    public int distinctCount;

    public static ColumnDetailDto from(ColumnMeta c) {
        ColumnDetailDto dto = new ColumnDetailDto();
        dto.id = c.getId();
        dto.columnName = c.getColumnName();
        dto.inferredType = c.getInferredType() != null ? c.getInferredType().name() : null;
        dto.sensitivityTag = c.getSensitivityTag() != null ? c.getSensitivityTag().name() : null;
        dto.tagOverridden = c.isTagOverridden();
        dto.missingPct = c.getMissingPct();
        dto.invalidCount = c.getInvalidCount();
        dto.distinctCount = c.getDistinctCount();
        return dto;
    }
}
