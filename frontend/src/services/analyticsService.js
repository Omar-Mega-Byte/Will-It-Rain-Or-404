import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

class AnalyticsService {
  constructor() {
    this.api = axios.create({
      baseURL: `${API_BASE_URL}/api/analytics`,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add request interceptor to include auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Add response interceptor for error handling
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // User Analytics
  async getCurrentUserAnalytics(days = 30) {
    try {
      const response = await this.api.get('/user', {
        params: { days }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching user analytics:', error);
      throw error;
    }
  }

  async getUserAnalyticsById(userId, days = 30) {
    try {
      const response = await this.api.get(`/user/${userId}`, {
        params: { days }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching user analytics by ID:', error);
      throw error;
    }
  }

  // System Metrics
  async getSystemHealth(hours = 24) {
    try {
      const response = await this.api.get('/system/health', {
        params: { hours }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching system health:', error);
      throw error;
    }
  }

  async getSystemMetrics(hours = 24) {
    try {
      const response = await this.api.get('/system/metrics', {
        params: { hours }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching system metrics:', error);
      throw error;
    }
  }

  // API Usage Analytics
  async getApiUsageStats(days = 7) {
    try {
      const response = await this.api.get('/api-usage', {
        params: { days }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching API usage stats:', error);
      throw error;
    }
  }

  // Prediction Analytics
  async getPredictionAccuracy(days = 30) {
    try {
      const response = await this.api.get('/predictions/accuracy', {
        params: { days }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching prediction accuracy:', error);
      throw error;
    }
  }

  // Generate Reports
  async generateAnalyticsReport(reportType, startDate, endDate, userIds = []) {
    try {
      const response = await this.api.post('/reports/generate', {
        reportType,
        startDate,
        endDate,
        userIds
      });
      return response.data;
    } catch (error) {
      console.error('Error generating analytics report:', error);
      throw error;
    }
  }

  // Export data
  async exportAnalyticsData(type, format = 'JSON', days = 30) {
    try {
      const response = await this.api.get(`/export/${type}`, {
        params: { format, days },
        responseType: format === 'CSV' ? 'blob' : 'json'
      });
      
      if (format === 'CSV') {
        // Handle CSV download
        const blob = new Blob([response.data], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${type}_analytics_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        return { success: true, message: 'Export completed' };
      }
      
      return response.data;
    } catch (error) {
      console.error('Error exporting analytics data:', error);
      throw error;
    }
  }
}

export default new AnalyticsService();