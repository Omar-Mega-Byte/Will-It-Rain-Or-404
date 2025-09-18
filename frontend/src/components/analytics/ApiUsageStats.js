import React, { useState, useEffect } from 'react';
import analyticsService from '../../services/analyticsService';
import StatCard from '../StatCard';
import './ApiUsageStats.css';

const ApiUsageStats = () => {
  const [usageData, setUsageData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [timeRange, setTimeRange] = useState('24h');
  const [selectedView, setSelectedView] = useState('overview');

  useEffect(() => {
    fetchUsageStats();
  }, [timeRange]);

  const fetchUsageStats = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const data = await analyticsService.getApiUsageStats(timeRange);
      setUsageData(data);
    } catch (err) {
      console.error('Error fetching API usage stats:', err);
      setError('Failed to load API usage statistics. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const getUsageStats = () => {
    if (!usageData) return [];
    
    return [
      {
        title: 'Total Requests',
        value: usageData.totalRequests || 0,
        change: usageData.requestsChangePercent ? `${usageData.requestsChangePercent > 0 ? '+' : ''}${usageData.requestsChangePercent}%` : 'N/A',
        trend: usageData.requestsChangePercent > 0 ? 'up' : usageData.requestsChangePercent < 0 ? 'down' : 'neutral',
        icon: '📊'
      },
      {
        title: 'Unique Users',
        value: usageData.uniqueUsers || 0,
        change: usageData.usersChangePercent ? `${usageData.usersChangePercent > 0 ? '+' : ''}${usageData.usersChangePercent}%` : 'N/A',
        trend: usageData.usersChangePercent > 0 ? 'up' : usageData.usersChangePercent < 0 ? 'down' : 'neutral',
        icon: '👥'
      },
      {
        title: 'Success Rate',
        value: usageData.successRate ? `${usageData.successRate}%` : 'N/A',
        change: usageData.successRateChange ? `${usageData.successRateChange > 0 ? '+' : ''}${usageData.successRateChange}%` : 'N/A',
        trend: usageData.successRateChange > 0 ? 'up' : usageData.successRateChange < 0 ? 'down' : 'neutral',
        icon: '✅'
      },
      {
        title: 'Avg Response Time',
        value: usageData.avgResponseTime ? `${usageData.avgResponseTime}ms` : 'N/A',
        change: usageData.responseTimeChange ? `${usageData.responseTimeChange > 0 ? '+' : ''}${usageData.responseTimeChange}ms` : 'N/A',
        trend: usageData.responseTimeChange > 0 ? 'down' : usageData.responseTimeChange < 0 ? 'up' : 'neutral',
        icon: '⚡'
      }
    ];
  };

  const getTopEndpoints = () => {
    return usageData?.topEndpoints || [
      { endpoint: '/api/weather/current', requests: 1250, avgTime: 145 },
      { endpoint: '/api/weather/forecast', requests: 980, avgTime: 201 },
      { endpoint: '/api/user/profile', requests: 756, avgTime: 89 },
      { endpoint: '/api/auth/login', requests: 432, avgTime: 156 },
      { endpoint: '/api/analytics/stats', requests: 234, avgTime: 267 }
    ];
  };

  const getErrorAnalysis = () => {
    return usageData?.errorBreakdown || [
      { code: '200', status: 'OK', count: 8945, percentage: 89.5 },
      { code: '404', status: 'Not Found', count: 567, percentage: 5.7 },
      { code: '401', status: 'Unauthorized', count: 234, percentage: 2.3 },
      { code: '500', status: 'Server Error', count: 156, percentage: 1.6 },
      { code: '429', status: 'Rate Limited', count: 98, percentage: 0.9 }
    ];
  };

  const getUsageInsights = () => {
    if (!usageData) return [];

    const insights = [];

    // High traffic insight
    if (usageData.totalRequests > 10000) {
      insights.push({
        type: 'info',
        icon: '📈',
        title: 'High Traffic Volume',
        message: `Your API received ${usageData.totalRequests} requests in the last ${timeRange}. Consider monitoring performance closely.`
      });
    }

    // Performance insight
    if (usageData.avgResponseTime > 1000) {
      insights.push({
        type: 'warning',
        icon: '🐌',
        title: 'Slow Response Times',
        message: `Average response time is ${usageData.avgResponseTime}ms. Consider optimizing your endpoints.`
      });
    }

    // Error rate insight
    if (usageData.errorRate > 5) {
      insights.push({
        type: 'critical',
        icon: '🚨',
        title: 'High Error Rate',
        message: `Error rate of ${usageData.errorRate}% is above recommended threshold of 5%.`
      });
    }

    // Success insight
    if (usageData.successRate > 95) {
      insights.push({
        type: 'success',
        icon: '🎯',
        title: 'Excellent Reliability',
        message: `Your API maintains a ${usageData.successRate}% success rate, which is excellent!`
      });
    }

    // Growth insight
    if (usageData.requestsChangePercent > 20) {
      insights.push({
        type: 'info',
        icon: '🚀',
        title: 'Rapid Growth',
        message: `API usage has grown ${usageData.requestsChangePercent}% compared to the previous period.`
      });
    }

    return insights;
  };

  if (loading) {
    return (
      <div className="api-usage-loading">
        <div className="loading-spinner">📊</div>
        <p>Loading API usage statistics...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="api-usage-error">
        <div className="error-icon">⚠️</div>
        <h3>Unable to Load API Usage Stats</h3>
        <p>{error}</p>
        <button className="retry-button" onClick={fetchUsageStats}>
          🔄 Retry
        </button>
      </div>
    );
  }

  const usageStats = getUsageStats();
  const topEndpoints = getTopEndpoints();
  const errorAnalysis = getErrorAnalysis();
  const insights = getUsageInsights();

  return (
    <div className="api-usage-stats">
      {/* Header */}
      <div className="usage-header">
        <div className="header-content">
          <h2>📊 API Usage Statistics</h2>
          <p>Monitor API performance and usage patterns</p>
        </div>
        <div className="header-controls">
          <div className="time-range-selector">
            <label>Time Range:</label>
            <select 
              value={timeRange} 
              onChange={(e) => setTimeRange(e.target.value)}
            >
              <option value="1h">Last Hour</option>
              <option value="24h">Last 24 Hours</option>
              <option value="7d">Last 7 Days</option>
              <option value="30d">Last 30 Days</option>
            </select>
          </div>
          <button className="refresh-button" onClick={fetchUsageStats}>
            🔄 Refresh
          </button>
        </div>
      </div>

      {/* Overview Stats */}
      <div className="stats-section">
        <h3>📈 Usage Overview</h3>
        <div className="stats-grid">
          {usageStats.map((stat, index) => (
            <StatCard key={index} {...stat} />
          ))}
        </div>
      </div>

      {/* View Selector */}
      <div className="view-selector">
        <h3>🔍 Detailed Analysis</h3>
        <div className="view-tabs">
          <button 
            className={selectedView === 'overview' ? 'active' : ''}
            onClick={() => setSelectedView('overview')}
          >
            Overview
          </button>
          <button 
            className={selectedView === 'endpoints' ? 'active' : ''}
            onClick={() => setSelectedView('endpoints')}
          >
            Top Endpoints
          </button>
          <button 
            className={selectedView === 'errors' ? 'active' : ''}
            onClick={() => setSelectedView('errors')}
          >
            Error Analysis
          </button>
          <button 
            className={selectedView === 'trends' ? 'active' : ''}
            onClick={() => setSelectedView('trends')}
          >
            Usage Trends
          </button>
        </div>
      </div>

      {/* Content based on selected view */}
      <div className="view-content">
        {selectedView === 'overview' && (
          <div className="overview-section">
            <div className="overview-grid">
              <div className="overview-card">
                <h4>🎯 Performance Summary</h4>
                <div className="performance-metrics">
                  <div className="metric-item">
                    <span className="metric-label">Requests/Hour:</span>
                    <span className="metric-value">{Math.round((usageData?.totalRequests || 0) / 24)}</span>
                  </div>
                  <div className="metric-item">
                    <span className="metric-label">Peak Hour:</span>
                    <span className="metric-value">{usageData?.peakHour || 'N/A'}</span>
                  </div>
                  <div className="metric-item">
                    <span className="metric-label">Data Transferred:</span>
                    <span className="metric-value">{usageData?.dataTransferred || 'N/A'}</span>
                  </div>
                </div>
              </div>
              
              <div className="overview-card">
                <h4>👥 User Behavior</h4>
                <div className="user-metrics">
                  <div className="metric-item">
                    <span className="metric-label">New Users:</span>
                    <span className="metric-value">{usageData?.newUsers || 0}</span>
                  </div>
                  <div className="metric-item">
                    <span className="metric-label">Returning Users:</span>
                    <span className="metric-value">{usageData?.returningUsers || 0}</span>
                  </div>
                  <div className="metric-item">
                    <span className="metric-label">Avg Session:</span>
                    <span className="metric-value">{usageData?.avgSessionDuration || 'N/A'}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {selectedView === 'endpoints' && (
          <div className="endpoints-section">
            <div className="endpoints-table">
              <h4>🔗 Most Popular Endpoints</h4>
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Endpoint</th>
                      <th>Requests</th>
                      <th>Avg Response Time</th>
                      <th>Success Rate</th>
                    </tr>
                  </thead>
                  <tbody>
                    {topEndpoints.map((endpoint, index) => (
                      <tr key={index}>
                        <td className="endpoint-name">{endpoint.endpoint}</td>
                        <td className="requests-count">{endpoint.requests?.toLocaleString()}</td>
                        <td className="response-time">{endpoint.avgTime}ms</td>
                        <td className="success-rate">
                          <span className={`rate-badge ${endpoint.successRate > 95 ? 'good' : endpoint.successRate > 90 ? 'average' : 'poor'}`}>
                            {endpoint.successRate || 95}%
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {selectedView === 'errors' && (
          <div className="errors-section">
            <div className="error-breakdown">
              <h4>🚨 HTTP Status Code Breakdown</h4>
              <div className="error-chart">
                {errorAnalysis.map((error, index) => (
                  <div key={index} className="error-bar-container">
                    <div className="error-info">
                      <span className="status-code">{error.code}</span>
                      <span className="status-text">{error.status}</span>
                      <span className="error-percentage">{error.percentage}%</span>
                    </div>
                    <div className="error-bar">
                      <div 
                        className={`bar-fill ${error.code.startsWith('2') ? 'success' : error.code.startsWith('4') ? 'client-error' : 'server-error'}`}
                        style={{ width: `${error.percentage}%` }}
                      ></div>
                    </div>
                    <span className="error-count">{error.count.toLocaleString()}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {selectedView === 'trends' && (
          <div className="trends-section">
            <div className="trends-chart">
              <h4>📈 Usage Trends Over Time</h4>
              <div className="trend-visualization">
                <div className="trend-placeholder">
                  <div className="chart-area">
                    <p>📊 Interactive usage charts would be displayed here</p>
                    <p>Showing request volume, response times, and error rates over time</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Usage Insights */}
      <div className="insights-section">
        <h3>💡 Usage Insights</h3>
        {insights.length > 0 ? (
          <div className="insights-grid">
            {insights.map((insight, index) => (
              <div key={index} className={`insight-card ${insight.type}`}>
                <div className="insight-header">
                  <span className="insight-icon">{insight.icon}</span>
                  <h4>{insight.title}</h4>
                </div>
                <p>{insight.message}</p>
              </div>
            ))}
          </div>
        ) : (
          <div className="no-insights">
            <span className="no-insights-icon">📊</span>
            <p>No specific insights available for the current time period.</p>
          </div>
        )}
      </div>

      {/* API Health Score */}
      <div className="health-score-section">
        <h3>🏥 API Health Score</h3>
        <div className="health-score-card">
          <div className="score-display">
            <div className="score-circle">
              <span className="score-value">{usageData?.healthScore || 85}</span>
              <span className="score-label">Health Score</span>
            </div>
            <div className="score-details">
              <div className="score-factor">
                <span className="factor-label">Performance:</span>
                <span className="factor-score">{usageData?.performanceScore || 90}/100</span>
              </div>
              <div className="score-factor">
                <span className="factor-label">Reliability:</span>
                <span className="factor-score">{usageData?.reliabilityScore || 95}/100</span>
              </div>
              <div className="score-factor">
                <span className="factor-label">Availability:</span>
                <span className="factor-score">{usageData?.availabilityScore || 99}/100</span>
              </div>
            </div>
          </div>
          <div className="score-recommendations">
            <h4>Recommendations:</h4>
            <ul>
              <li>Monitor response times during peak hours</li>
              <li>Implement caching for frequently requested endpoints</li>
              <li>Set up alerts for error rate spikes</li>
              <li>Consider API rate limiting for better resource management</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ApiUsageStats;