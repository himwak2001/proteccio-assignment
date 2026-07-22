package com.proteccio.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "column_meta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ColumnMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "column_name")
    private String columnName;

    @Column(name = "ordinal_position")
    private int ordinalPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "inferred_type")
    private DataType inferredType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity_tag")
    private SensitivityTag sensitivityTag = SensitivityTag.NONE;

    @Column(name = "tag_overridden")
    private boolean tagOverridden = false;

    @Column(name = "missing_pct")
    private double missingPct;

    @Column(name = "invalid_count")
    private int invalidCount;

    @Column(name = "distinct_count")
    private int distinctCount;
}
