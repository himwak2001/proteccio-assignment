import React, { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getDatasetDetail, overrideColumnTag } from "../api/api.js";

const SENSITIVITY_TAGS = [
  "NONE",
  "EMAIL",
  "PHONE_NUMBER",
  "PERSON_NAME",
  "ID_NUMBER",
  "ADDRESS",
  "DATE_OF_BIRTH",
  "FINANCIAL",
  "OTHER_SENSITIVE",
];

export default function DatasetDetail() {
  const { id } = useParams();
  const [dataset, setDataset] = useState(null);
  const [error, setError] = useState(null);

  const fetchDetail = useCallback(() => {
    getDatasetDetail(id)
      .then(setDataset)
      .catch(() => setError("Could not load this dataset"));
  }, [id]);

  useEffect(() => {
    fetchDetail();
  }, [fetchDetail]);

  async function handleTagChange(columnId, newTag) {
    try {
      await overrideColumnTag(id, columnId, newTag);
      fetchDetail(); // refresh so trust score reflects the change
    } catch {
      setError("Could not update the tag");
    }
  }

  if (error) return <p className="form-error">{error}</p>;
  if (!dataset) return <p>Loading...</p>;

  return (
    <div className="dataset-detail">
      <Link to="/" className="back-link">
        &larr; Back to catalog
      </Link>
      <h2>{dataset.filename}</h2>

      <div className="metric-grid">
        <div className="metric-card">
          <span className="metric-label">Rows x Columns</span>
          <span className="metric-value">
            {dataset.rowCount} x {dataset.columnCount}
          </span>
        </div>
        <div className="metric-card">
          <span className="metric-label">Quality score</span>
          <span className="metric-value">
            {dataset.qualityScore.toFixed(1)}
          </span>
        </div>
        <div className="metric-card">
          <span className="metric-label">Trust score</span>
          <span className="metric-value">{dataset.trustScore.toFixed(1)}</span>
        </div>
        <div className="metric-card">
          <span className="metric-label">Views</span>
          <span className="metric-value">{dataset.viewCount}</span>
        </div>
        <div className="metric-card">
          <span className="metric-label">Duplicate rows</span>
          <span className="metric-value">{dataset.duplicateRowCount}</span>
        </div>
        <div className="metric-card">
          <span className="metric-label">Invalid values</span>
          <span className="metric-value">{dataset.invalidValueCount}</span>
        </div>
      </div>

      <h3>Columns</h3>
      <table className="column-table">
        <thead>
          <tr>
            <th>Column</th>
            <th>Inferred type</th>
            <th>Sensitivity tag</th>
            <th>Missing %</th>
            <th>Invalid</th>
            <th>Distinct</th>
          </tr>
        </thead>
        <tbody>
          {dataset.columns.map((col) => (
            <tr key={col.id}>
              <td>{col.columnName}</td>
              <td>{col.inferredType}</td>
              <td>
                <select
                  value={col.sensitivityTag}
                  onChange={(e) => handleTagChange(col.id, e.target.value)}
                  className={col.tagOverridden ? "tag-overridden" : ""}
                >
                  {SENSITIVITY_TAGS.map((tag) => (
                    <option key={tag} value={tag}>
                      {tag}
                    </option>
                  ))}
                </select>
              </td>
              <td>{col.missingPct.toFixed(1)}%</td>
              <td>{col.invalidCount}</td>
              <td>{col.distinctCount}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
