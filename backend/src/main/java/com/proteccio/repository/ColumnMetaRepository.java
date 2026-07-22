package com.proteccio.repository;

import com.proteccio.model.ColumnMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnMetaRepository extends JpaRepository<ColumnMeta, Long> {
    List<ColumnMeta> findByDatasetIdOrderByOrdinalPosition(Long datasetId);
}
