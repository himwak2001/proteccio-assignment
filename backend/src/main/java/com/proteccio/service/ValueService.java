package com.proteccio.service;

import com.proteccio.model.Dataset;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ValueService {
    private static final int LOW_ACTIVITY_DAYS = 30;

    public void recordView(Dataset dataset) {
        dataset.setViewCount(dataset.getViewCount() + 1);
        dataset.setLastViewedAt(LocalDateTime.now());
        dataset.setLowActivity(false);
    }

    public void refreshActivityFlag(Dataset dataset) {
        LocalDateTime reference = dataset.getLastViewedAt() != null ? dataset.getLastViewedAt() : dataset.getUploadTime();
        long daysSinceActivity = ChronoUnit.DAYS.between(reference, LocalDateTime.now());
        dataset.setLowActivity(daysSinceActivity > LOW_ACTIVITY_DAYS);
    }
}
