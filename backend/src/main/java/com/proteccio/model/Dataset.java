package com.proteccio.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "datasets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String filename;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Column(name = "row_count")
    private int rowCount;

    @Column(name = "column_count")
    private int columnCount;

    @Column(name = "quality_score")
    private double qualityScore;

    @Column(name = "missing_value_pct")
    private double missingValuePct;

    @Column(name = "duplicate_row_count")
    private int duplicateRowCount;

    @Column(name = "invalid_value_count")
    private int invalidValueCount;

    @Column(name = "trust_score")
    private double trustScore;

    @Column(name = "view_count")
    private long viewCount = 0;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "low_activity")
    private boolean lowActivity = false;

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColumnMeta> columns = new ArrayList<>();
}
