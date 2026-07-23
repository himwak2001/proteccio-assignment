package com.proteccio.service;

import com.proteccio.model.Dataset;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ValueService {
    private static final int LOW_ACTIVITY_DAYS = 30;

    /**
     * Call this whenever someone opens a dataset's detail page. Bumps the view
     * count and clears the low-activity flag since it's clearly being looked at now.
     *
     * @param dataset the dataset being viewed - gets mutated, caller saves it
     */
    public void recordView(Dataset dataset) {
        dataset.setViewCount(dataset.getViewCount() + 1);
        dataset.setLastViewedAt(LocalDateTime.now());
        dataset.setLowActivity(false);
    }

    /**
     * Recalculates whether a dataset counts as low-activity, without registering
     * a new view. Used right after upload so a freshly-uploaded dataset isn't
     * immediately flagged just because it hasn't been clicked on yet.
     *
     * @param dataset the dataset to check - gets mutated, caller saves it
     */
    public void refreshActivityFlag(Dataset dataset) {
        LocalDateTime reference = dataset.getLastViewedAt() != null ? dataset.getLastViewedAt() : dataset.getUploadTime();
        long daysSinceActivity = ChronoUnit.DAYS.between(reference, LocalDateTime.now());
        dataset.setLowActivity(daysSinceActivity > LOW_ACTIVITY_DAYS);
    }
}
