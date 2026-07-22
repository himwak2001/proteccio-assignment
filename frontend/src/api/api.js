import axios from "axios";

// point this at your deployed backend via VITE_API_BASE_URL, falls back to
// localhost for dev. vite only exposes env vars prefixed with VITE_
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const client = axios.create({ baseURL: API_BASE_URL });

export const listDatasets = () =>
  client.get("/datasets").then((res) => res.data);

export const getDatasetDetail = (id) =>
  client.get(`/datasets/${id}`).then((res) => res.data);

export const uploadDataset = (file, onUploadProgress) => {
  const formData = new FormData();
  formData.append("file", file);
  return client
    .post("/datasets/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      onUploadProgress,
    })
    .then((res) => res.data);
};

export const overrideColumnTag = (datasetId, columnId, sensitivityTag) =>
  client
    .patch(`/datasets/${datasetId}/columns/${columnId}`, { sensitivityTag })
    .then((res) => res.data);

export default client;
