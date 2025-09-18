import React from 'react';
import './ActivityFeed.css';

const ActivityFeed = () => {
  const activities = [
    {
      id: 1,
      type: 'weather_alert',
      title: 'Weather Alert',
      description: 'Heavy rain expected in your area',
      time: '2 minutes ago',
      icon: '⚡',
      priority: 'high'
    },
    {
      id: 2,
      type: 'forecast_update',
      title: 'Forecast Updated',
      description: 'Temperature dropping to 15°C tonight',
      time: '15 minutes ago',
      icon: '🌡️',
      priority: 'medium'
    },
    {
      id: 3,
      type: 'user_login',
      title: 'Profile Access',
      description: 'You logged in from new device',
      time: '1 hour ago',
      icon: '👤',
      priority: 'low'
    },
    {
      id: 4,
      type: 'location_update',
      title: 'Location Updated',
      description: 'Weather data refreshed for current location',
      time: '2 hours ago',
      icon: '📍',
      priority: 'medium'
    },
    {
      id: 5,
      type: 'system_maintenance',
      title: 'System Update',
      description: 'Weather prediction model updated',
      time: '3 hours ago',
      icon: '🔧',
      priority: 'low'
    }
  ];

  const getPriorityColor = (priority) => {
    switch (priority) {
      case 'high':
        return '#ef4444';
      case 'medium':
        return '#f59e0b';
      case 'low':
        return '#10b981';
      default:
        return '#6b7280';
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'weather_alert':
        return 'linear-gradient(135deg, #ff6b6b, #ff8e8e)';
      case 'forecast_update':
        return 'linear-gradient(135deg, #4ecdc4, #44a08d)';
      case 'user_login':
        return 'linear-gradient(135deg, #667eea, #764ba2)';
      case 'location_update':
        return 'linear-gradient(135deg, #f093fb, #f5576c)';
      case 'system_maintenance':
        return 'linear-gradient(135deg, #4facfe, #00f2fe)';
      default:
        return 'linear-gradient(135deg, #a8edea, #fed6e3)';
    }
  };

  return (
    <div className="activity-feed">
      <div className="feed-header">
        <h3>Recent Activity</h3>
        <button className="view-all-btn">
          View All
        </button>
      </div>

      <div className="activities-list">
        {activities.map((activity) => (
          <div key={activity.id} className="activity-item">
            <div 
              className="activity-icon"
              style={{ background: getTypeColor(activity.type) }}
            >
              <span>{activity.icon}</span>
            </div>
            
            <div className="activity-content">
              <div className="activity-header">
                <h4 className="activity-title">{activity.title}</h4>
                <div 
                  className="priority-indicator"
                  style={{ backgroundColor: getPriorityColor(activity.priority) }}
                ></div>
              </div>
              
              <p className="activity-description">{activity.description}</p>
              <span className="activity-time">{activity.time}</span>
            </div>
          </div>
        ))}
      </div>

      <div className="feed-footer">
        <button className="refresh-btn">
          <span>🔄</span>
          Refresh
        </button>
        <div className="activity-summary">
          <span className="summary-text">
            {activities.length} recent activities
          </span>
        </div>
      </div>
    </div>
  );
};

export default ActivityFeed;