import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import LoadingSpinner from '../LoadingSpinner';
import StatCard from '../StatCard';
import analyticsService from '../../services/analyticsService';
import './AnalyticsOverview.css';

const AnalyticsOverview = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState({
    userAnalytics: null,
    systemHealth: null,
    apiUsage: null,
    predictionAccuracy: null
  });
  const [error, setError] = useState(null);
  const [user, setUser] = useState(null);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      setUser(JSON.parse(userData));
    }
    loadOverviewData();
  }, []);

  const loadOverviewData = async () => {
    setLoading(true);
    setError(null);

    try {
      const promises = [
        analyticsService.getCurrentUserAnalytics(7),
        analyticsService.getPredictionAccuracy(7)
      ];

      // Add admin-only data if user is admin
      const userData = JSON.parse(localStorage.getItem('user') || '{}');
      if (userData.role === 'ADMIN' || userData.roles?.includes('ADMIN')) {
        promises.push(
          analyticsService.getSystemHealth(24),
          analyticsService.getApiUsageStats(7)
        );
      }

      const results = await Promise.allSettled(promises);
      
      setData({
        userAnalytics: results[0].status === 'fulfilled' ? results[0].value : null,
        predictionAccuracy: results[1].status === 'fulfilled' ? results[1].value : null,
        systemHealth: results[2]?.status === 'fulfilled' ? results[2].value : null,
        apiUsage: results[3]?.status === 'fulfilled' ? results[3].value : null
      });

      // Log any errors but don't fail the whole component
      results.forEach((result, index) => {
        if (result.status === 'rejected') {
          console.warn(`Failed to load data for index ${index}:`, result.reason);
        }
      });

    } catch (err) {
      console.error('Error loading overview data:', err);
      setError('Failed to load analytics overview');
    } finally {
      setLoading(false);
    }
  };

  const isUserAdmin = () => {
    return user?.role === 'ADMIN' || user?.roles?.includes('ADMIN');
  };

  const getQuickStats = () => {
    const stats = [];

    // User stats
    if (data.userAnalytics) {
      stats.push({
        title: 'Your Activity (7d)',
        value: data.userAnalytics.totalSessions || 0,
        unit: 'sessions',
        icon: '👤',
        trend: data.userAnalytics.sessionGrowth || 0,
        color: '#667eea'
      });

      stats.push({
        title: 'Weather Queries',
        value: data.userAnalytics.totalQueries || 0,
        unit: 'queries',
        icon: '🌤️',
        trend: data.userAnalytics.queryGrowth || 0,
        color: '#f093fb'
      });
    }

    // Prediction accuracy
    if (data.predictionAccuracy) {
      stats.push({
        title: 'Prediction Accuracy',
        value: data.predictionAccuracy.overallAccuracy || 0,
        unit: '%',
        icon: '🎯',
        trend: data.predictionAccuracy.accuracyTrend || 0,
        color: '#4facfe'
      });
    }

    // System health (admin only)
    if (data.systemHealth && isUserAdmin()) {
      stats.push({
        title: 'System Health',
        value: data.systemHealth.healthScore || 0,
        unit: '%',
        icon: '⚙️',
        trend: data.systemHealth.healthTrend || 0,
        color: '#43e97b'
      });
    }

    return stats;
  };

  const getRecentInsights = () => {
    const insights = [];

    if (data.userAnalytics) {
      insights.push({
        type: 'user',
        title: 'Your Weather Journey',
        description: `You've been quite active lately! ${data.userAnalytics.totalSessions || 0} sessions in the past week.`,
        action: 'View Details',
        actionPath: '/analytics/user',
        icon: '📈',
        color: '#667eea'
      });
    }

    if (data.predictionAccuracy) {
      const accuracy = data.predictionAccuracy.overallAccuracy || 0;
      insights.push({
        type: 'accuracy',
        title: 'Prediction Performance',
        description: `Our weather predictions are ${accuracy.toFixed(1)}% accurate this week. ${accuracy > 85 ? 'Excellent!' : 'Room for improvement.'}`,
        action: 'View Accuracy',
        actionPath: '/analytics/predictions',
        icon: '🎯',
        color: '#4facfe'
      });
    }

    if (data.systemHealth && isUserAdmin()) {
      const health = data.systemHealth.healthScore || 0;
      insights.push({
        type: 'system',
        title: 'System Status',
        description: `System is running at ${health.toFixed(1)}% capacity. ${health > 90 ? 'All systems optimal!' : 'Monitor required.'}`,
        action: 'View Metrics',
        actionPath: '/analytics/system',
        icon: '⚙️',
        color: '#43e97b'
      });
    }

    if (data.apiUsage && isUserAdmin()) {
      insights.push({
        type: 'api',
        title: 'API Performance',
        description: `${data.apiUsage.totalRequests || 0} API requests processed this week with ${(data.apiUsage.averageResponseTime || 0).toFixed(0)}ms avg response time.`,
        action: 'View Usage',
        actionPath: '/analytics/api-usage',
        icon: '🔌',
        color: '#fa709a'
      });
    }

    return insights;
  };

  if (loading) {
    return (
      <div className="overview-loading">
        <LoadingSpinner />
        <p>Loading analytics overview...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="overview-error">
        <div className="error-icon">⚠️</div>
        <h3>Unable to Load Analytics</h3>
        <p>{error}</p>
        <button onClick={loadOverviewData} className="retry-button">
          Try Again
        </button>
      </div>
    );
  }

  const quickStats = getQuickStats();
  const insights = getRecentInsights();

  return (
    <div className="analytics-overview">
      <div className="overview-header">
        <div className="header-content">
          <h2>Welcome back{user?.username ? `, ${user.username}` : ''}!</h2>
          <p>Here's what's happening with your weather analytics</p>
        </div>
        <div className="header-actions">
          <button 
            onClick={loadOverviewData} 
            className="refresh-button"
            disabled={loading}
          >
            🔄 Refresh
          </button>
        </div>
      </div>

      {/* Quick Stats Grid */}
      <div className="quick-stats-section">
        <h3>📊 Quick Stats</h3>
        <div className="stats-grid">
          {quickStats.map((stat, index) => (
            <StatCard
              key={index}
              title={stat.title}
              value={stat.value}
              unit={stat.unit}
              icon={stat.icon}
              trend={stat.trend}
              color={stat.color}
            />
          ))}
        </div>
      </div>

      {/* Recent Insights */}
      <div className="insights-section">
        <h3>💡 Recent Insights</h3>
        <div className="insights-grid">
          {insights.map((insight, index) => (
            <div 
              key={index} 
              className="insight-card"
              style={{ borderLeft: `4px solid ${insight.color}` }}
            >
              <div className="insight-header">
                <span className="insight-icon">{insight.icon}</span>
                <h4>{insight.title}</h4>
              </div>
              <p>{insight.description}</p>
              <button 
                onClick={() => navigate(insight.actionPath)}
                className="insight-action"
                style={{ color: insight.color }}
              >
                {insight.action} →
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Quick Navigation */}
      <div className="quick-nav-section">
        <h3>🚀 Quick Navigation</h3>
        <div className="quick-nav-grid">
          <div className="nav-card" onClick={() => navigate('/analytics/user')}>
            <span className="nav-icon">👤</span>
            <h4>Your Analytics</h4>
            <p>View your personal usage patterns</p>
          </div>
          
          <div className="nav-card" onClick={() => navigate('/analytics/predictions')}>
            <span className="nav-icon">🎯</span>
            <h4>Predictions</h4>
            <p>Check forecast accuracy metrics</p>
          </div>

          {isUserAdmin() && (
            <>
              <div className="nav-card" onClick={() => navigate('/analytics/system')}>
                <span className="nav-icon">⚙️</span>
                <h4>System Health</h4>
                <p>Monitor system performance</p>
              </div>
              
              <div className="nav-card" onClick={() => navigate('/analytics/reports')}>
                <span className="nav-icon">📈</span>
                <h4>Generate Reports</h4>
                <p>Create custom analytics reports</p>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default AnalyticsOverview;