package com.proteccio.service;

import com.proteccio.model.ColumnMeta;
import com.proteccio.model.SensitivityTag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrustService {
    /**
     * Trust score = how much can we rely on this data. Mostly driven by quality
     * (70%), with the rest coming from how much of it has actually been reviewed
     * for sensitive content (30%). A dataset that's never been classified is
     * treated as less trustworthy even if the data itself is clean, because we
     * don't actually know if it's hiding unflagged PII.
     *
     * @param qualityScore the dataset's quality score, already computed
     * @param columns      all columns for this dataset, used to check classification status
     * @return trust score, 0-100
     */
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
