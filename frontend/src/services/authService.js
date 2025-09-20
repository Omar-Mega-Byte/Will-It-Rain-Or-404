import axios from 'axios';

const API_URL = '/api/auth';

// Flag to prevent multiple refresh attempts
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

// Function to get CSRF token from cookies
const getCsrfToken = () => {
  const name = 'XSRF-TOKEN=';
  const decodedCookie = decodeURIComponent(document.cookie);
  const cookies = decodedCookie.split(';');
  for (let cookie of cookies) {
    cookie = cookie.trim();
    if (cookie.indexOf(name) === 0) {
      return cookie.substring(name.length);
    }
  }
  return null;
};

// Set up axios interceptor to include JWT token and CSRF token in requests
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // ...existing code...

    // Ensure Content-Type is application/json for all requests
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json';
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Enhanced response interceptor with automatic token refresh
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // If already refreshing, queue this request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          originalRequest.headers['Authorization'] = 'Bearer ' + token;
          return axios(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = localStorage.getItem('token');
        if (refreshToken) {
          const response = await axios.post(`${API_URL}/refresh`, {
            refreshToken: refreshToken
          });

          const { token } = response.data;
          localStorage.setItem('token', token);
          localStorage.setItem('user', JSON.stringify(response.data.user));

          processQueue(null, token);
          originalRequest.headers['Authorization'] = 'Bearer ' + token;

          return axios(originalRequest);
        }
      } catch (refreshError) {
        processQueue(refreshError, null);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      } finally {
        isRefreshing = false;
      }
    }

    // For non-401 errors or if refresh failed
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }

    return Promise.reject(error);
  }
);

const authService = {
  async login(username, password) {
    try {
      console.log('Sending login request for user:', username);

      const response = await axios.post(`${API_URL}/login`, {
        username,
        password,
      }, {
        headers: {
          'Content-Type': 'application/json',
        },
      });

      console.log('Login response:', response.data);

      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify(response.data.user));
      }

      return response.data;
    } catch (error) {
      console.error('Login error:', error);
      console.error('Login error response:', error.response?.data);

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Login failed';
      throw errorMessage;
    }
  },

  async register(username, email, password, firstName, lastName) {
    try {
      const userData = {
        username,
        email,
        password,
        firstName,
        lastName,
        role: 'USER' // Default role for new registrations
      };

      console.log('Sending registration request with data:', userData);

      const response = await axios.post(`${API_URL}/register`, userData, {
        headers: {
          'Content-Type': 'application/json',
        },
      });

      console.log('Registration response:', response.data);

      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify(response.data.user));
      }

      return response.data;
    } catch (error) {
      console.error('Registration error:', error);
      console.error('Registration error response:', error.response?.data);

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Registration failed';
      throw errorMessage;
    }
  },

  async logout() {
    try {
      // Call backend logout endpoint to invalidate token
      await axios.post(`${API_URL}/logout`);
    } catch (error) {
      console.error('Logout error:', error);
      // Continue with local logout even if backend call fails
    } finally {
      // Always clear local storage
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  },

  async refreshToken() {
    try {
      const currentToken = this.getToken();
      if (!currentToken) {
        throw new Error('No token available to refresh');
      }

      const response = await axios.post(`${API_URL}/refresh`, {
        refreshToken: currentToken
      });

      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify(response.data.user));
      }

      return response.data;
    } catch (error) {
      console.error('Token refresh error:', error);

      // If refresh fails, logout user
      this.logout();
      window.location.href = '/login';

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Token refresh failed';
      throw errorMessage;
    }
  },

  async forgotPassword(email) {
    try {
      const response = await axios.post(`${API_URL}/forgot-password`, {
        email
      });

      return response.data;
    } catch (error) {
      console.error('Forgot password error:', error);

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to send password reset email';
      throw errorMessage;
    }
  },

  async resetPassword(token, newPassword) {
    try {
      const response = await axios.post(`${API_URL}/reset-password`, {
        token,
        newPassword
      });

      return response.data;
    } catch (error) {
      console.error('Reset password error:', error);

      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to reset password';
      throw errorMessage;
    }
  },

  getCurrentUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  getToken() {
    return localStorage.getItem('token');
  },

  isAuthenticated() {
    const token = this.getToken();
    const user = this.getCurrentUser();
    return !!(token && user);
  }
};

export default authService;
