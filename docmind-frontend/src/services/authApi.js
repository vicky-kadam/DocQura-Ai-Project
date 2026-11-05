import axios from 'axios';

// Raw (non-enveloped) endpoints; bare axios so no auth interceptor / circular import.
export const authApi = {
  login: (username, password) =>
    axios.post('/api/v1/auth/login', { username, password }).then((r) => r.data),
  register: (username, email, password) =>
    axios.post('/api/v1/auth/register', { username, email, password }).then((r) => r.data),
};
