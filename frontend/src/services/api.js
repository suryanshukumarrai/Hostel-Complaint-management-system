import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const getAuthHeader = (user) => {
  if (!user) return {};
  const token = btoa(`${user.username}:${user.password}`);
  return { Authorization: `Basic ${token}` };
};

const api = axios.create({
  baseURL: API_BASE_URL,
});

export { getAuthHeader };
export default api;
