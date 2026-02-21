import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

export const dashboardService = {
  getAdminStats: async (credentials) => {
    const response = await axios.get(`${BASE_URL}/admin/dashboard/stats`, {
      headers: {
        Authorization: `Basic ${credentials}`
      }
    });
    return response.data;
  }
  ,
  exportComplaints: async (credentials) => {
    const response = await axios.get(`${BASE_URL}/complaints/export-all`, {
      headers: {
        Authorization: `Basic ${credentials}`
      },
      responseType: 'blob'
    });
    return response;
  }
};
