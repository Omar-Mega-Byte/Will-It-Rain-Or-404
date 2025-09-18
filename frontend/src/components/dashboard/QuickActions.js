import React from 'react';
import './QuickActions.css';

const QuickActions = () => {
  const actions = [
    {
      id: 1,
      title: 'Create Event',
      description: 'Schedule a new weather monitoring event',
      icon: '➕',
      color: '#667eea',
      action: () => console.log('Create event')
    },
    {
      id: 2,
      title: 'Weather Report',
      description: 'Generate detailed weather analysis',
      icon: '📊',
      color: '#10b981',
      action: () => console.log('Generate report')
    },
    {
      id: 3,
      title: 'Set Alert',
      description: 'Configure weather notification alerts',
      icon: '🔔',
      color: '#f59e0b',
      action: () => console.log('Set alert')
    },
    {
      id: 4,
      title: 'Export Data',
      description: 'Download weather data and analytics',
      icon: '📥',
      color: '#3b82f6',
      action: () => console.log('Export data')
    }
  ];

  return (
    <div className="quick-actions">
      <div className="widget-header">
        <h3>⚡ Quick Actions</h3>
      </div>

      <div className="actions-grid">
        {actions.map((action) => (
          <button
            key={action.id}
            className="action-button"
            onClick={action.action}
            style={{
              '--action-color': action.color,
              '--action-color-light': `${action.color}20`
            }}
          >
            <div className="action-icon">{action.icon}</div>
            <div className="action-content">
              <div className="action-title">{action.title}</div>
              <div className="action-description">{action.description}</div>
            </div>
            <div className="action-arrow">→</div>
          </button>
        ))}
      </div>

      <div className="actions-footer">
        <button className="view-all-actions">
          View All Actions
        </button>
      </div>
    </div>
  );
};

export default QuickActions;