import React, { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listDatasets } from "../api/api.js";
import UploadForm from "./UploadForm.jsx";

function scoreClass(score) {
  if (score >= 80) return "score-good";
  if (score >= 50) return "score-medium";
  return "score-poor";
}

export default function Dashboard() {
  const [datasets, setDatasets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDatasets = useCallback(() => {
    setLoading(true);
    listDatasets()
      .then(setDatasets)
      .catch(() => setError("Could not load datasets, is the backend running?"))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchDatasets();
  }, [fetchDatasets]);

  return (
    <div className="dashboard">
      <section className="upload-section">
        <h2>Upload a dataset</h2>
        <p className="hint">
          CSV or Excel. Columns, sensitive fields, and scores get worked out
          automatically.
        </p>
        <UploadForm onUploaded={fetchDatasets} />
      </section>

      <section className="catalog-section">
        <h2>Catalog ({datasets.length})</h2>

        {loading && <p>Loading...</p>}
        {error && <p className="form-error">{error}</p>}
        {!loading && !error && datasets.length === 0 && (
          <p className="empty-state">
            No datasets yet, upload one above to get started.
          </p>
        )}

        {!loading && datasets.length > 0 && (
          <table className="catalog-table">
            <thead>
              <tr>
                <th>Filename</th>
                <th>Uploaded</th>
                <th>Rows x Cols</th>
                <th>Sensitive cols</th>
                <th>Quality</th>
                <th>Trust</th>
                <th>Views</th>
                <th>Activity</th>
              </tr>
            </thead>
            <tbody>
              {datasets.map((d) => (
                <tr key={d.id}>
                  <td>
                    <Link to={`/datasets/${d.id}`}>{d.filename}</Link>
                  </td>
                  <td>{new Date(d.uploadTime).toLocaleString()}</td>
                  <td>
                    {d.rowCount} x {d.columnCount}
                  </td>
                  <td>{d.sensitiveColumnCount}</td>
                  <td className={scoreClass(d.qualityScore)}>
                    {d.qualityScore.toFixed(1)}
                  </td>
                  <td className={scoreClass(d.trustScore)}>
                    {d.trustScore.toFixed(1)}
                  </td>
                  <td>{d.viewCount}</td>
                  <td>
                    {d.lowActivity ? (
                      <span className="badge-low">Low activity</span>
                    ) : (
                      <span className="badge-active">Active</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
