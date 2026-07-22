package com.proteccio.service;

import com.proteccio.model.ColumnMeta;
import com.proteccio.model.SensitivityTag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrustService {
    public double computeTrustScore(double qualityScore, List<ColumnMeta> columns) {
        if (columns.isEmpty()) return qualityScore * 0.7;

        long reviewedOrTagged = columns.stream()
                .filter(c -> c.isTagOverridden() || c.getSensitivityTag() != SensitivityTag.NONE)
                .count();

        double classificationCompleteness = (double) reviewedOrTagged / columns.size() * 100.0;
        double trust = qualityScore * 0.7 + classificationCompleteness * 0.3;

        return Math.max(0.0, Math.min(100.0, trust));
    }
}
