import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import AnalyticsOverview from './AnalyticsOverview';
import UserAnalytics from './UserAnalytics';
import SystemMetrics from './SystemMetrics';
import ApiUsageStats from './ApiUsageStats';
import PredictionAnalytics from './PredictionAnalytics';
import './Analytics.css';

const Analytics = () => {
  const [userRole, setUserRole] = useState('USER');
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Get user role from localStorage or context
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    
    if (!token) {
      navigate('/login');
      return;
    }

    try {
      const userData = user ? JSON.parse(user) : null;
      setUserRole(userData?.role || 'USER');
    } catch (error) {
      console.error('Error parsing user data:', error);
      setUserRole('USER');
    }
  }, [navigate]);

  const getActiveTab = () => {
    const path = location.pathname;
    if (path.includes('/user')) return 'user';
    if (path.includes('/system')) return 'system';
    if (path.includes('/api-usage')) return 'api-usage';
    if (path.includes('/predictions')) return 'predictions';
    return 'overview';
  };

  const handleTabClick = (tab) => {
    navigate(`/analytics/${tab === 'overview' ? '' : tab}`);
  };

  const isAdmin = userRole === 'ADMIN';

  return (
    <div className="analytics-container">
      {/* Header */}
      <div className="analytics-header">
        <div className="header-content">
          <h1>📊 Analytics Dashboard</h1>
          <p>Monitor performance, usage, and insights</p>
        </div>
        <div className="user-badge">
          <span className="role-badge" data-role={userRole.toLowerCase()}>
            {userRole === 'ADMIN' ? '👑' : '👤'} {userRole}
          </span>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="analytics-nav">
        <button 
          className={`nav-tab ${getActiveTab() === 'overview' ? 'active' : ''}`}
          onClick={() => handleTabClick('overview')}
        >
          <span className="tab-icon">🏠</span>
          Overview
        </button>
        
        <button 
          className={`nav-tab ${getActiveTab() === 'user' ? 'active' : ''}`}
          onClick={() => handleTabClick('user')}
        >
          <span className="tab-icon">👤</span>
          My Analytics
        </button>

        {isAdmin && (
          <>
            <button 
              className={`nav-tab ${getActiveTab() === 'system' ? 'active' : ''}`}
              onClick={() => handleTabClick('system')}
            >
              <span className="tab-icon">🖥️</span>
              System Metrics
            </button>
            
            <button 
              className={`nav-tab ${getActiveTab() === 'api-usage' ? 'active' : ''}`}
              onClick={() => handleTabClick('api-usage')}
            >
              <span className="tab-icon">📊</span>
              API Usage
            </button>
            
            <button 
              className={`nav-tab ${getActiveTab() === 'predictions' ? 'active' : ''}`}
              onClick={() => handleTabClick('predictions')}
            >
              <span className="tab-icon">🔮</span>
              Predictions
            </button>
          </>
        )}
      </div>

      {/* Content Area */}
      <div className="analytics-content">
        <Routes>
          <Route path="/" element={<AnalyticsOverview />} />
          <Route path="/user" element={<UserAnalytics />} />
          
          {/* Admin-only routes */}
          {isAdmin ? (
            <>
              <Route path="/system" element={<SystemMetrics />} />
              <Route path="/api-usage" element={<ApiUsageStats />} />
              <Route path="/predictions" element={<PredictionAnalytics />} />
            </>
          ) : (
            <>
              <Route path="/system" element={<AccessDenied />} />
              <Route path="/api-usage" element={<AccessDenied />} />
              <Route path="/predictions" element={<AccessDenied />} />
            </>
          )}
          
          {/* Catch all */}
          <Route path="*" element={<Navigate to="/analytics" replace />} />
        </Routes>
      </div>
    </div>
  );
};

// Access Denied Component for non-admin users
const AccessDenied = () => {
  const navigate = useNavigate();

  return (
    <div className="access-denied">
      <div className="access-denied-content">
        <div className="access-denied-icon">🔒</div>
        <h2>Access Denied</h2>
        <p>You need administrator privileges to access this section.</p>
        <button 
          className="back-button"
          onClick={() => navigate('/analytics')}
        >
          ← Back to Overview
        </button>
      </div>
    </div>
  );
};

export default Analytics;