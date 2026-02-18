import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

const getAuthHeader = (user) => {
  if (!user) return {};
  const token = user.credentials || btoa(`${user.username}:${user.password || ''}`);
  return { Authorization: `Basic ${token}` };
};

export const getAllUsers = async (currentUser) => {
  const response = await axios.get(`${BASE_URL}/users`, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};

export const getUserById = async (id, currentUser) => {
  const response = await axios.get(`${BASE_URL}/users/${id}`, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};
