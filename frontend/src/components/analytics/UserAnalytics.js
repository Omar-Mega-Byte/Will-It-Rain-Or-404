import React, { useState, useEffect } from 'react';
import LoadingSpinner from '../LoadingSpinner';
import StatCard from '../StatCard';
import analyticsService from '../../services/analyticsService';
import './UserAnalytics.css';

const UserAnalytics = () => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [timeRange, setTimeRange] = useState(30);
  const [selectedMetric, setSelectedMetric] = useState('sessions');

  useEffect(() => {
    loadUserAnalytics();
  }, [timeRange]);

  const loadUserAnalytics = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await analyticsService.getCurrentUserAnalytics(timeRange);
      setData(response);
    } catch (err) {
      console.error('Error loading user analytics:', err);
      setError('Failed to load user analytics data');
    } finally {
      setLoading(false);
    }
  };

  const timeRangeOptions = [
    { value: 7, label: '7 days' },
    { value: 30, label: '30 days' },
    { value: 90, label: '90 days' },
    { value: 365, label: '1 year' }
  ];

  const getUserStats = () => {
    if (!data) return [];

    return [
      {
        title: 'Total Sessions',
        value: data.totalSessions || 0,
        unit: 'sessions',
        icon: '🔄',
        trend: data.sessionGrowth || 0,
        color: '#667eea',
        description: 'Number of times you accessed the weather app'
      },
      {
        title: 'Weather Queries',
        value: data.totalQueries || 0,
        unit: 'queries',
        icon: '🌤️',
        trend: data.queryGrowth || 0,
        color: '#f093fb',
        description: 'Total weather lookups performed'
      },
      {
        title: 'Avg Session Time',
        value: data.averageSessionDuration || 0,
        unit: 'min',
        icon: '⏱️',
        trend: data.sessionDurationTrend || 0,
        color: '#4facfe',
        description: 'Average time spent per session'
      },
      {
        title: 'Favorite Locations',
        value: data.uniqueLocations || 0,
        unit: 'cities',
        icon: '📍',
        trend: data.locationTrend || 0,
        color: '#43e97b',
        description: 'Different cities you\'ve searched'
      }
    ];
  };

  const getActivityInsights = () => {
    if (!data) return [];

    const insights = [];

    // Peak usage time
    if (data.peakUsageHour !== undefined) {
      const hour = data.peakUsageHour;
      const timeStr = hour === 0 ? '12 AM' : hour <= 12 ? `${hour} AM` : `${hour - 12} PM`;
      insights.push({
        title: 'Peak Activity Time',
        description: `You're most active around ${timeStr}`,
        icon: '⏰',
        color: '#667eea'
      });
    }

    // Most searched location
    if (data.topLocation) {
      insights.push({
        title: 'Favorite Location',
        description: `${data.topLocation} is your most searched city`,
        icon: '🏙️',
        color: '#f093fb'
      });
    }

    // Usage pattern
    if (data.usagePattern) {
      insights.push({
        title: 'Usage Pattern',
        description: `You tend to check weather ${data.usagePattern}`,
        icon: '📊',
        color: '#4facfe'
      });
    }

    // Accuracy preference
    if (data.accuracyPreference) {
      insights.push({
        title: 'Forecast Preference',
        description: `You prefer ${data.accuracyPreference} forecasts`,
        icon: '🎯',
        color: '#43e97b'
      });
    }

    return insights;
  };

  const getChartData = () => {
    if (!data || !data.dailyActivity) return [];

    return data.dailyActivity.map(item => ({
      date: new Date(item.date).toLocaleDateString(),
      sessions: item.sessions || 0,
      queries: item.queries || 0,
      duration: item.duration || 0
    }));
  };

  const renderSimpleChart = () => {
    const chartData = getChartData();
    if (chartData.length === 0) return null;

    const maxValue = Math.max(...chartData.map(d => d[selectedMetric]));
    
    return (
      <div className="simple-chart">
        <div className="chart-header">
          <h4>Activity Over Time</h4>
          <div className="metric-selector">
            <button 
              className={selectedMetric === 'sessions' ? 'active' : ''}
              onClick={() => setSelectedMetric('sessions')}
            >
              Sessions
            </button>
            <button 
              className={selectedMetric === 'queries' ? 'active' : ''}
              onClick={() => setSelectedMetric('queries')}
            >
              Queries
            </button>
            <button 
              className={selectedMetric === 'duration' ? 'active' : ''}
              onClick={() => setSelectedMetric('duration')}
            >
              Duration
            </button>
          </div>
        </div>
        
        <div className="chart-container">
          {chartData.map((item, index) => (
            <div key={index} className="chart-bar-container">
              <div 
                className="chart-bar"
                style={{ 
                  height: `${(item[selectedMetric] / maxValue) * 100}%`,
                  backgroundColor: selectedMetric === 'sessions' ? '#667eea' :
                                 selectedMetric === 'queries' ? '#f093fb' : '#4facfe'
                }}
                title={`${item.date}: ${item[selectedMetric]} ${selectedMetric}`}
              />
              <span className="chart-label">{item.date.split('/')[1]}/{item.date.split('/')[2]}</span>
            </div>
          ))}
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="user-analytics-loading">
        <LoadingSpinner />
        <p>Loading your analytics...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="user-analytics-error">
        <div className="error-icon">⚠️</div>
        <h3>Unable to Load Your Analytics</h3>
        <p>{error}</p>
        <button onClick={loadUserAnalytics} className="retry-button">
          Try Again
        </button>
      </div>
    );
  }

  const userStats = getUserStats();
  const insights = getActivityInsights();

  return (
    <div className="user-analytics">
      <div className="analytics-header">
        <div className="header-content">
          <h2>👤 Your Weather Analytics</h2>
          <p>Insights into your weather app usage patterns</p>
        </div>
        
        <div className="header-controls">
          <div className="time-range-selector">
            <label>Time Range:</label>
            <select 
              value={timeRange} 
              onChange={(e) => setTimeRange(Number(e.target.value))}
            >
              {timeRangeOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          
          <button onClick={loadUserAnalytics} className="refresh-button">
            🔄 Refresh
          </button>
        </div>
      </div>

      {/* User Statistics */}
      <div className="stats-section">
        <h3>📊 Your Statistics</h3>
        <div className="stats-grid">
          {userStats.map((stat, index) => (
            <StatCard
              key={index}
              title={stat.title}
              value={stat.value}
              unit={stat.unit}
              icon={stat.icon}
              trend={stat.trend}
              color={stat.color}
              description={stat.description}
            />
          ))}
        </div>
      </div>

      {/* Activity Chart */}
      <div className="chart-section">
        <h3>📈 Activity Trends</h3>
        <div className="chart-card">
          {renderSimpleChart()}
        </div>
      </div>

      {/* Usage Insights */}
      <div className="insights-section">
        <h3>💡 Usage Insights</h3>
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
            </div>
          ))}
          
          {insights.length === 0 && (
            <div className="no-insights">
              <span className="no-insights-icon">🤔</span>
              <p>Not enough data to generate insights yet. Keep using the app to see your patterns!</p>
            </div>
          )}
        </div>
      </div>

      {/* Personal Recommendations */}
      <div className="recommendations-section">
        <h3>🎯 Personal Recommendations</h3>
        <div className="recommendations-grid">
          <div className="recommendation-card">
            <span className="rec-icon">🌅</span>
            <h4>Morning Routine</h4>
            <p>Based on your usage, you check weather most in the morning. Consider enabling morning notifications!</p>
          </div>
          
          <div className="recommendation-card">
            <span className="rec-icon">📱</span>
            <h4>Mobile Experience</h4>
            <p>Add our app to your home screen for quick access to weather updates throughout the day.</p>
          </div>
          
          <div className="recommendation-card">
            <span className="rec-icon">🎨</span>
            <h4>Customize Widgets</h4>
            <p>Set up weather widgets for your favorite locations to get instant updates without opening the app.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserAnalytics;