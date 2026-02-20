import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

const getAuthHeader = (user) => {
  if (!user) return {};
  // credentials is already base64-encoded by authService.login
  const token = user.credentials || btoa(`${user.username}:${user.password || ''}`);
  return { Authorization: `Basic ${token}` };
};

export const getAllComplaints = async (currentUser) => {
  const response = await axios.get(`${BASE_URL}/complaints`, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};

export const getComplaintById = async (id, currentUser) => {
  const response = await axios.get(`${BASE_URL}/complaints/${id}`, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};

export const createComplaint = async (payload, currentUser, imageFile) => {
  const formData = new FormData();
  Object.entries(payload).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      formData.append(key, value);
    }
  });

  if (imageFile) {
    formData.append('image', imageFile);
  }

  const response = await axios.post(`${BASE_URL}/complaints`, formData, {
    headers: {
      ...getAuthHeader(currentUser),
      // Do NOT set Content-Type manually; let axios set the multipart boundary
    },
  });
  return response.data;
};

export const updateComplaintStatus = async (id, status, currentUser) => {
  const response = await axios.put(`${BASE_URL}/complaints/${id}/status`, { status }, {
    headers: getAuthHeader(currentUser),
  });
  return response.data;
};
