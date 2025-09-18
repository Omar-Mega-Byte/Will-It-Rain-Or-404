import React from 'react';
import './StatsGrid.css';

const StatsGrid = () => {
  const stats = [
    {
      id: 1,
      title: 'Total Events',
      value: '1,247',
      change: '+12%',
      trend: 'up',
      icon: '📅',
      color: '#667eea'
    },
    {
      id: 2,
      title: 'Active Alerts',
      value: '23',
      change: '+5%',
      trend: 'up',
      icon: '🚨',
      color: '#f59e0b'
    },
    {
      id: 3,
      title: 'Weather Accuracy',
      value: '94.2%',
      change: '+2.1%',
      trend: 'up',
      icon: '🎯',
      color: '#10b981'
    },
    {
      id: 4,
      title: 'Response Time',
      value: '0.8s',
      change: '-15%',
      trend: 'down',
      icon: '⚡',
      color: '#3b82f6'
    }
  ];

  const getTrendIcon = (trend) => {
    return trend === 'up' ? '📈' : '📉';
  };

  const getTrendClass = (trend) => {
    return trend === 'up' ? 'trend-up' : 'trend-down';
  };

  return (
    <div className="stats-grid">
      {stats.map((stat) => (
        <div key={stat.id} className="stat-card">
          <div className="stat-header">
            <div 
              className="stat-icon"
              style={{ backgroundColor: `${stat.color}20`, color: stat.color }}
            >
              {stat.icon}
            </div>
            <div className={`stat-change ${getTrendClass(stat.trend)}`}>
              {getTrendIcon(stat.trend)} {stat.change}
            </div>
          </div>
          
          <div className="stat-content">
            <div className="stat-value" style={{ color: stat.color }}>
              {stat.value}
            </div>
            <div className="stat-title">{stat.title}</div>
          </div>

          <div className="stat-progress">
            <div 
              className="progress-bar"
              style={{ 
                backgroundColor: `${stat.color}20`,
                '--progress-color': stat.color,
                '--progress-width': '70%'
              }}
            >
              <div className="progress-fill"></div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default StatsGrid;