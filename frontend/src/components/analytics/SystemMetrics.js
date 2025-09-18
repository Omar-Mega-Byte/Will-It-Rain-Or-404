import React, { useState, useEffect } from 'react';
import analyticsService from '../../services/analyticsService';
import StatCard from '../StatCard';
import './SystemMetrics.css';

const SystemMetrics = () => {
  const [systemData, setSystemData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [timeRange, setTimeRange] = useState('24h');
  const [selectedMetric, setSelectedMetric] = useState('all');

  useEffect(() => {
    fetchSystemMetrics();
  }, [timeRange]);

  const fetchSystemMetrics = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [healthData, usageData] = await Promise.all([
        analyticsService.getSystemHealth(),
        analyticsService.getApiUsageStats(timeRange)
      ]);

      setSystemData({
        health: healthData,
        usage: usageData
      });
    } catch (err) {
      console.error('Error fetching system metrics:', err);
      setError('Failed to load system metrics. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const renderHealthStatus = (status) => {
    const statusConfig = {
      healthy: { icon: '🟢', color: '#10b981', label: 'Healthy' },
      warning: { icon: '🟡', color: '#f59e0b', label: 'Warning' },
      critical: { icon: '🔴', color: '#ef4444', label: 'Critical' },
      unknown: { icon: '⚪', color: '#6b7280', label: 'Unknown' }
    };

    const config = statusConfig[status?.toLowerCase()] || statusConfig.unknown;
    
    return (
      <span className="health-status" style={{ color: config.color }}>
        {config.icon} {config.label}
      </span>
    );
  };

  const getSystemStats = () => {
    if (!systemData) return [];

    const { health, usage } = systemData;
    
    return [
      {
        title: 'System Status',
        value: renderHealthStatus(health?.overallStatus),
        change: health?.uptime ? `${health.uptime} uptime` : 'N/A',
        trend: 'neutral',
        icon: '🖥️'
      },
      {
        title: 'API Requests',
        value: usage?.totalRequests || 0,
        change: usage?.requestsChangePercent ? `${usage.requestsChangePercent > 0 ? '+' : ''}${usage.requestsChangePercent}%` : 'N/A',
        trend: usage?.requestsChangePercent > 0 ? 'up' : usage?.requestsChangePercent < 0 ? 'down' : 'neutral',
        icon: '📊'
      },
      {
        title: 'Error Rate',
        value: usage?.errorRate ? `${usage.errorRate}%` : '0%',
        change: usage?.errorRateChange ? `${usage.errorRateChange > 0 ? '+' : ''}${usage.errorRateChange}%` : 'N/A',
        trend: usage?.errorRateChange > 0 ? 'down' : usage?.errorRateChange < 0 ? 'up' : 'neutral',
        icon: '⚠️'
      },
      {
        title: 'Avg Response Time',
        value: usage?.avgResponseTime ? `${usage.avgResponseTime}ms` : 'N/A',
        change: usage?.responseTimeChange ? `${usage.responseTimeChange > 0 ? '+' : ''}${usage.responseTimeChange}ms` : 'N/A',
        trend: usage?.responseTimeChange > 0 ? 'down' : usage?.responseTimeChange < 0 ? 'up' : 'neutral',
        icon: '⚡'
      }
    ];
  };

  const getPerformanceInsights = () => {
    if (!systemData) return [];

    const insights = [];
    const { health, usage } = systemData;

    // System health insights
    if (health?.cpuUsage > 80) {
      insights.push({
        type: 'warning',
        icon: '🔥',
        title: 'High CPU Usage',
        message: `CPU usage is at ${health.cpuUsage}%. Consider scaling resources.`
      });
    }

    if (health?.memoryUsage > 85) {
      insights.push({
        type: 'critical',
        icon: '💾',
        title: 'Memory Warning',
        message: `Memory usage is at ${health.memoryUsage}%. Memory optimization needed.`
      });
    }

    // API performance insights
    if (usage?.errorRate > 5) {
      insights.push({
        type: 'critical',
        icon: '🚨',
        title: 'High Error Rate',
        message: `Error rate of ${usage.errorRate}% detected. Investigation required.`
      });
    }

    if (usage?.avgResponseTime > 2000) {
      insights.push({
        type: 'warning',
        icon: '🐌',
        title: 'Slow Response Times',
        message: `Average response time is ${usage.avgResponseTime}ms. Performance optimization needed.`
      });
    }

    // Positive insights
    if (health?.overallStatus === 'healthy' && usage?.errorRate < 1) {
      insights.push({
        type: 'success',
        icon: '✅',
        title: 'System Performing Well',
        message: 'All systems are running smoothly with low error rates.'
      });
    }

    return insights;
  };

  if (loading) {
    return (
      <div className="system-metrics-loading">
        <div className="loading-spinner">⏳</div>
        <p>Loading system metrics...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="system-metrics-error">
        <div className="error-icon">⚠️</div>
        <h3>Unable to Load System Metrics</h3>
        <p>{error}</p>
        <button className="retry-button" onClick={fetchSystemMetrics}>
          🔄 Retry
        </button>
      </div>
    );
  }

  const systemStats = getSystemStats();
  const insights = getPerformanceInsights();

  return (
    <div className="system-metrics">
      {/* Header */}
      <div className="metrics-header">
        <div className="header-content">
          <h2>🖥️ System Metrics</h2>
          <p>Monitor system health and performance metrics</p>
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
          <button className="refresh-button" onClick={fetchSystemMetrics}>
            🔄 Refresh
          </button>
        </div>
      </div>

      {/* System Overview Stats */}
      <div className="stats-section">
        <h3>📊 System Overview</h3>
        <div className="stats-grid">
          {systemStats.map((stat, index) => (
            <StatCard key={index} {...stat} />
          ))}
        </div>
      </div>

      {/* Detailed Metrics */}
      <div className="detailed-metrics-section">
        <h3>🔍 Detailed Metrics</h3>
        <div className="metrics-tabs">
          <button 
            className={selectedMetric === 'all' ? 'active' : ''}
            onClick={() => setSelectedMetric('all')}
          >
            All Metrics
          </button>
          <button 
            className={selectedMetric === 'performance' ? 'active' : ''}
            onClick={() => setSelectedMetric('performance')}
          >
            Performance
          </button>
          <button 
            className={selectedMetric === 'resources' ? 'active' : ''}
            onClick={() => setSelectedMetric('resources')}
          >
            Resources
          </button>
          <button 
            className={selectedMetric === 'errors' ? 'active' : ''}
            onClick={() => setSelectedMetric('errors')}
          >
            Errors
          </button>
        </div>

        <div className="metrics-content">
          {(selectedMetric === 'all' || selectedMetric === 'performance') && (
            <div className="metric-group">
              <h4>⚡ Performance Metrics</h4>
              <div className="metric-grid">
                <div className="metric-card">
                  <div className="metric-value">
                    {systemData?.usage?.avgResponseTime || 'N/A'}
                    <span className="metric-unit">ms</span>
                  </div>
                  <div className="metric-label">Avg Response Time</div>
                </div>
                <div className="metric-card">
                  <div className="metric-value">
                    {systemData?.usage?.throughput || 'N/A'}
                    <span className="metric-unit">req/s</span>
                  </div>
                  <div className="metric-label">Throughput</div>
                </div>
                <div className="metric-card">
                  <div className="metric-value">
                    {systemData?.health?.diskUsage || 'N/A'}
                    <span className="metric-unit">%</span>
                  </div>
                  <div className="metric-label">Disk Usage</div>
                </div>
              </div>
            </div>
          )}

          {(selectedMetric === 'all' || selectedMetric === 'resources') && (
            <div className="metric-group">
              <h4>💾 Resource Usage</h4>
              <div className="metric-grid">
                <div className="metric-card">
                  <div className="metric-value">
                    {systemData?.health?.cpuUsage || 'N/A'}
                    <span className="metric-unit">%</span>
                  </div>
                  <div className="metric-label">CPU Usage</div>
                </div>
                <div className="metric-card">
                  <div className="metric-value">
                    {systemData?.health?.memoryUsage || 'N/A'}
                    <span className="metric-unit">%</span>
                  </div>
                  <div className="metric-label">Memory Usage</div>
                </div>
                <div className="metric-card">
                  <div className="metric-value">
                    {systemData?.health?.activeConnections || 'N/A'}
                  </div>
                  <div className="metric-label">Active Connections</div>
                </div>
              </div>
            </div>
          )}

          {(selectedMetric === 'all' || selectedMetric === 'errors') && (
            <div className="metric-group">
              <h4>🚨 Error Tracking</h4>
              <div className="metric-grid">
                <div className="metric-card">
                  <div className="metric-value">
                    {systemData?.usage?.errorRate || '0'}
                    <span className="metric-unit">%</span>
                  </div>
                  <div className="metric-label">Error Rate</div>
                </div>
                <div className="metric-card">
                  <div className="metric-value">
                    {systemData?.usage?.totalErrors || '0'}
                  </div>
                  <div className="metric-label">Total Errors</div>
                </div>
                <div className="metric-card">
                  <div className="metric-value">
                    {systemData?.health?.lastError ? '❌' : '✅'}
                  </div>
                  <div className="metric-label">Last Error Status</div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Performance Insights */}
      <div className="insights-section">
        <h3>💡 Performance Insights</h3>
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
            <span className="no-insights-icon">📈</span>
            <p>No performance insights available at the moment.</p>
          </div>
        )}
      </div>

      {/* System Health Summary */}
      <div className="health-summary-section">
        <h3>🏥 Health Summary</h3>
        <div className="health-summary-card">
          <div className="health-overview">
            <div className="health-status-large">
              {renderHealthStatus(systemData?.health?.overallStatus)}
            </div>
            <div className="health-details">
              <p><strong>Uptime:</strong> {systemData?.health?.uptime || 'Unknown'}</p>
              <p><strong>Last Check:</strong> {systemData?.health?.lastCheck || 'Never'}</p>
              <p><strong>Version:</strong> {systemData?.health?.version || 'Unknown'}</p>
            </div>
          </div>
          
          {systemData?.health?.services && (
            <div className="services-status">
              <h4>Service Status</h4>
              <div className="services-grid">
                {Object.entries(systemData.health.services).map(([service, status]) => (
                  <div key={service} className="service-item">
                    <span className="service-name">{service}</span>
                    {renderHealthStatus(status)}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SystemMetrics;