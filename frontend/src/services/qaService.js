import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

const getAuthHeader = (user) => {
  if (!user) return {};
  const token = user.credentials || btoa(`${user.username}:${user.password || ''}`);
  return { Authorization: `Basic ${token}` };
};

export const askClientQuestion = async (question, userId, currentUser) => {
  const response = await axios.post(`${BASE_URL}/clients/qa`, {
    question,
    userId,
  }, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};

export const askAdminQuestion = async (question, currentUser) => {
  const response = await axios.post(`${BASE_URL}/admin/qa`, {
    question,
    userId: currentUser?.userId,
  }, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};

export const getQaHistory = async (currentUser) => {
  if (!currentUser?.userId) return [];
  const response = await axios.get(`${BASE_URL}/qa/history/${currentUser.userId}`, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};

export const getUserAiAnalytics = async (currentUser) => {
  if (!currentUser?.userId) return null;
  const response = await axios.get(`${BASE_URL}/qa/history/analytics/user/${currentUser.userId}`, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};

export const getGlobalAiAnalytics = async (currentUser) => {
  const response = await axios.get(`${BASE_URL}/qa/history/analytics/global`, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};

export const getUserAiDailyCounts = async (currentUser, days = 7) => {
  if (!currentUser?.userId) return [];
  const response = await axios.get(`${BASE_URL}/qa/history/analytics/user/${currentUser.userId}/daily`, {
    params: { days },
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};

export const getGlobalAiDailyCounts = async (currentUser, days = 7) => {
  const response = await axios.get(`${BASE_URL}/qa/history/analytics/global/daily`, {
    params: { days },
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};
