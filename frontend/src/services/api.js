import axios from 'axios';
import { authService } from './authService';

const API_BASE_URL = 'http://localhost:8080/api';

const getAuthHeader = (user) => {
  if (!user) return {};
  const token = btoa(`${user.username}:${user.password}`);
  return { Authorization: `Basic ${token}` };
};

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add interceptor to include authorization header on all requests
api.interceptors.request.use(
  (config) => {
    const credentials = authService.getStoredCredentials();
    if (credentials) {
      config.headers.Authorization = `Basic ${credentials}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle 401 responses (unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear stored credentials on 401
      authService.logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export { getAuthHeader };
export default api;
