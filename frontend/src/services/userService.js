import axios from 'axios';

const API_URL = '/api/users';

const userService = {
  async getUserProfile() {
    try {
      const response = await axios.get(`${API_URL}/profile`);
      return response.data;
    } catch (error) {
      console.error('Get profile error:', error);

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to fetch user profile';
      throw errorMessage;
    }
  },

  async updateUserProfile(profileData) {
    try {
      const response = await axios.put(`${API_URL}/profile`, profileData);

      // Update local storage with new user data
      localStorage.setItem('user', JSON.stringify(response.data));

      return response.data;
    } catch (error) {
      console.error('Update profile error:', error);

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to update user profile';
      throw errorMessage;
    }
  },

  async getUserPreferences() {
    try {
      const response = await axios.get(`${API_URL}/preferences`);
      return response.data;
    } catch (error) {
      console.error('Get preferences error:', error);

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to fetch user preferences';
      throw errorMessage;
    }
  },

  async updateUserPreferences(preferencesData) {
    try {
      const response = await axios.put(`${API_URL}/preferences`, preferencesData);
      return response.data;
    } catch (error) {
      console.error('Update preferences error:', error);

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to update user preferences';
      throw errorMessage;
    }
  },

  async deleteUserAccount() {
    try {
      const response = await axios.delete(`${API_URL}/account`);

      // Clear local storage after successful account deletion
      localStorage.removeItem('token');
      localStorage.removeItem('user');

      return response.data;
    } catch (error) {
      console.error('Delete account error:', error);

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to delete user account';
      throw errorMessage;
    }
  }
};

export default userService;
