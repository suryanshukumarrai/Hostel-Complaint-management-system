import api from './api';

/**
 * Production-grade AI Complaint Service
 * Handles structured error parsing, user messaging, and retry logic
 */
export const aiComplaintService = {
  /**
   * Generate complaint using AI from free-form description
   * @param {string} description - Free-form complaint description (max 10000 chars)
   * @returns {Promise<Object>} Structured complaint with category, priority, team assignment
   * @throws {Error} With user-friendly message property
   */
  generateComplaint: async (description) => {
    if (!description || description.trim().length === 0) {
      throw createUserError('Please describe your complaint before submitting.', 'VALIDATION_ERROR');
    }

    if (description.length > 10000) {
      throw createUserError('Complaint description must be under 10,000 characters.', 'VALIDATION_ERROR');
    }

    try {
      const response = await api.post('/ai/generate-complaint', { description });
      
      if (!response?.data) {
        throw createUserError(
          'Invalid response from server. Please try again.',
          'INVALID_RESPONSE'
        );
      }

      return response.data;
    } catch (error) {
      if (error.userMessage) {
        throw error;
      }
      throw handleApiError(error);
    }
  },
};

/**
 * Parse backend error response and create user-friendly error
 * @param {Error} error - Axios error object
 * @returns {Error} Error with userMessage property for UI display
 */
function handleApiError(error) {
  const defaultMessage = 'Failed to generate complaint. Please check your connection and try again.';

  if (!error.response) {
    if (error.code === 'ENOTFOUND' || error.code === 'ECONNREFUSED') {
      return createUserError(
        'Server is unavailable. Please check if the backend is running and try again.',
        'CONNECTION_ERROR'
      );
    }

    if (error.code === 'ECONNABORTED') {
      return createUserError(
        'Request timed out. Please check your connection and try again.',
        'TIMEOUT_ERROR'
      );
    }

    return createUserError(defaultMessage, 'NETWORK_ERROR');
  }

  const { status, data } = error.response;

  if (status === 0) {
    return createUserError(
      'Network error. Please check your internet connection.',
      'NETWORK_ERROR'
    );
  }

  if (status === 400) {
    const message = data?.message || 'Invalid request. Please check your input and try again.';
    return createUserError(message, 'VALIDATION_ERROR');
  }

  if (status === 401 || status === 403) {
    return createUserError(
      'Your session has expired. Please log in again.',
      'AUTH_ERROR'
    );
  }

  if (status === 404) {
    return createUserError(
      'Server endpoint not found. Backend may need restart.',
      'NOT_FOUND'
    );
  }

  if (status === 500) {
    const message = data?.message || 'Server error occurred. Please try again later.';
    if (message.includes('API key')) {
      return createUserError(
        'AI service is not properly configured. Please contact support.',
        'CONFIG_ERROR'
      );
    }
    if (message.includes('Model') || message.includes('endpoint')) {
      return createUserError(
        'AI model endpoint error. Please contact support.',
        'MODEL_ERROR'
      );
    }
    return createUserError(message, 'SERVER_ERROR');
  }

  if (status >= 500) {
    return createUserError(
      'Server error. Please try again later.',
      'SERVER_ERROR'
    );
  }

  return createUserError(defaultMessage, 'API_ERROR');
}

/**
 * Create error object with userMessage for UI display
 * @param {string} userMessage - User-friendly error message
 * @param {string} errorCode - Machine-readable error code
 * @returns {Error} Error with properties
 */
function createUserError(userMessage, errorCode) {
  const error = new Error(userMessage);
  error.userMessage = userMessage;
  error.errorCode = errorCode;
  return error;
}
