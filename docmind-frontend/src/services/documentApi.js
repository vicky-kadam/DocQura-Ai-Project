import api from './api';

export const documentApi = {
  getMine: () => api.get('').then((r) => r.data.data), // GET /api  (auth aane par '/user' jaisa kuch)
  getAll: () => api.get('').then((r) => r.data.data),
  getById: (id) => api.get(`/${id}`).then((r) => r.data.data),
  remove: (id) => api.delete(`/${id}`).then((r) => r.data),
  upload: (file) => {
    const fd = new FormData();
    fd.append('file', file);
    return api.post('/upload', fd).then((r) => r.data.data);
  },
  uploadMultiple: (files) => {
    const fd = new FormData();
    files.forEach((f) => fd.append('files', f));
    return api.post('/upload-multiple', fd).then((r) => r.data.data);
  },
};