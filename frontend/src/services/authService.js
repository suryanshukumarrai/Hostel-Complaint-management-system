import api from './api';

export const authService = {
  signup: async (username, password, fullName, email, contactNumber) => {
    const response = await api.post('/auth/signup', {
      username,
      password,
      fullName,
      email,
      contactNumber
    });
    return response.data;
  },

  login: async (username, password) => {
    const credentials = btoa(`${username}:${password}`);
    // Call login with Basic Auth header so Spring Security authenticates
    const response = await api.post('/auth/login', {}, {
      headers: { Authorization: `Basic ${credentials}` }
    });
    // Store credentials and full user info
    localStorage.setItem('authCredentials', credentials);
    localStorage.setItem('username', username);
    localStorage.setItem('userInfo', JSON.stringify(response.data));
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('authCredentials');
    localStorage.removeItem('username');
    localStorage.removeItem('userInfo');
  },

  getStoredCredentials: () => {
    return localStorage.getItem('authCredentials');
  },

  isAuthenticated: () => {
    return !!localStorage.getItem('authCredentials');
  },

  getCurrentUsername: () => {
    return localStorage.getItem('username');
  },

  getUserInfo: () => {
    try {
      return JSON.parse(localStorage.getItem('userInfo') || 'null');
    } catch { return null; }
  }
};

