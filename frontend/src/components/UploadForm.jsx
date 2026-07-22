import React, { useState } from "react";
import { uploadDataset } from "../api/api.js";

export default function UploadForm({ onUploaded }) {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!file) return;

    setUploading(true);
    setError(null);
    try {
      await uploadDataset(file, (evt) => {
        if (evt.total) setProgress(Math.round((evt.loaded * 100) / evt.total));
      });
      setFile(null);
      setProgress(0);
      onUploaded();
    } catch (err) {
      setError(
        err?.response?.data?.error ||
          "Upload failed, check the file and try again",
      );
    } finally {
      setUploading(false);
    }
  }

  return (
    <form className="upload-form" onSubmit={handleSubmit}>
      <input
        type="file"
        accept=".csv,.xlsx,.xls"
        onChange={(e) => setFile(e.target.files[0])}
        disabled={uploading}
      />
      <button type="submit" disabled={!file || uploading}>
        {uploading ? `Uploading... ${progress}%` : "Upload dataset"}
      </button>
      {error && <p className="form-error">{error}</p>}
    </form>
  );
}
