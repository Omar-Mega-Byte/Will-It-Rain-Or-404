import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import WeatherWidget from '../components/dashboard/WeatherWidget';
import StatsGrid from '../components/dashboard/StatsGrid';
import ActivityFeed from '../components/dashboard/ActivityFeed';
import QuickActions from '../components/dashboard/QuickActions';
import WeatherChart from '../components/dashboard/WeatherChart';
import UpcomingEvents from '../components/UpcomingEvents';
import './ModernDashboard.css';

const ModernDashboard = () => {
  const [user, setUser] = useState(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [currentTime, setCurrentTime] = useState(new Date());
  const [notifications, setNotifications] = useState([
    { id: 1, type: 'weather', message: 'Storm warning issued for your area', time: '5 min ago' },
    { id: 2, type: 'event', message: 'Weather monitoring event starts in 1 hour', time: '15 min ago' }
  ]);
  const navigate = useNavigate();

  useEffect(() => {
    // Check authentication
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    // Get user data
    const userData = {
      username: 'John Doe',
      email: 'john.doe@example.com',
      role: 'USER',
      avatar: '👤',
      joinDate: '2024-01-15'
    };
    setUser(userData);

    // Update time every minute
    const timeInterval = setInterval(() => {
      setCurrentTime(new Date());
    }, 60000);

    return () => clearInterval(timeInterval);
  }, [navigate]);

  const handleLogout = () => {
    authService.logout();
    navigate('/');
  };

  const formatTime = (date) => {
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  };

  const formatDate = (date) => {
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (!user) {
    return (
      <div className="dashboard-loading">
        <div className="loading-animation">
          <div className="spinner"></div>
          <p>Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="modern-dashboard">
      {/* Sidebar */}
      <aside className={`dashboard-sidebar ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="sidebar-header">
          <div className="brand">
            <span className="brand-icon">🌦️</span>
            <h2 className="brand-name">WeatherVision</h2>
          </div>
          <button 
            className="sidebar-toggle"
            onClick={() => setSidebarOpen(!sidebarOpen)}
          >
            {sidebarOpen ? '✕' : '☰'}
          </button>
        </div>

        <nav className="sidebar-nav">
          <Link to="/dashboard" className="nav-item active">
            <span className="nav-icon">🏠</span>
            <span className="nav-text">Dashboard</span>
          </Link>
          <Link to="/events" className="nav-item">
            <span className="nav-icon">📅</span>
            <span className="nav-text">Events</span>
          </Link>
          <Link to="/calendar" className="nav-item">
            <span className="nav-icon">🗓️</span>
            <span className="nav-text">Calendar</span>
          </Link>
          <Link to="/analytics" className="nav-item">
            <span className="nav-icon">📊</span>
            <span className="nav-text">Analytics</span>
          </Link>
          <div className="nav-divider"></div>
          <Link to="/profile" className="nav-item">
            <span className="nav-icon">👤</span>
            <span className="nav-text">Profile</span>
          </Link>
          <Link to="/settings" className="nav-item">
            <span className="nav-icon">⚙️</span>
            <span className="nav-text">Settings</span>
          </Link>
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">{user.avatar}</div>
            <div className="user-details">
              <div className="user-name">{user.username}</div>
              <div className="user-email">{user.email}</div>
            </div>
          </div>
          <button className="logout-btn" onClick={handleLogout}>
            <span className="logout-icon">🚪</span>
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="dashboard-main">
        {/* Top Bar */}
        <header className="dashboard-header">
          <div className="header-left">
            <button 
              className="mobile-menu-btn"
              onClick={() => setSidebarOpen(!sidebarOpen)}
            >
              ☰
            </button>
            <div className="page-title">
              <h1>Dashboard</h1>
              <p>Welcome back, {user.username}!</p>
            </div>
          </div>

          <div className="header-right">
            <div className="datetime-display">
              <div className="current-time">{formatTime(currentTime)}</div>
              <div className="current-date">{formatDate(currentTime)}</div>
            </div>
            
            <div className="notifications">
              <button className="notification-btn">
                <span className="notification-icon">🔔</span>
                {notifications.length > 0 && (
                  <span className="notification-badge">{notifications.length}</span>
                )}
              </button>
            </div>

            <div className="user-menu">
              <div className="user-avatar-small">{user.avatar}</div>
            </div>
          </div>
        </header>

        {/* Dashboard Content */}
        <div className="dashboard-content">
          {/* Stats Overview */}
          <section className="stats-section">
            <StatsGrid />
          </section>

          {/* Main Grid */}
          <div className="dashboard-grid">
            {/* Weather Widget */}
            <div className="widget-card weather-widget">
              <WeatherWidget />
            </div>

            {/* Weather Chart */}
            <div className="widget-card chart-widget">
              <WeatherChart />
            </div>

            {/* Quick Actions */}
            <div className="widget-card actions-widget">
              <QuickActions />
            </div>

            {/* Upcoming Events */}
            <div className="widget-card events-widget">
              <div className="widget-header">
                <h3>📅 Upcoming Events</h3>
                <Link to="/events" className="view-all-link">View All</Link>
              </div>
              <UpcomingEvents limit={5} />
            </div>

            {/* Activity Feed */}
            <div className="widget-card activity-widget">
              <ActivityFeed />
            </div>

            {/* Weather Alerts */}
            <div className="widget-card alerts-widget">
              <div className="widget-header">
                <h3>⚠️ Weather Alerts</h3>
                <span className="alert-count">2 Active</span>
              </div>
              <div className="alerts-list">
                <div className="alert-item warning">
                  <div className="alert-icon">🌩️</div>
                  <div className="alert-content">
                    <div className="alert-title">Thunderstorm Warning</div>
                    <div className="alert-description">
                      Severe thunderstorms expected in your area between 3-6 PM
                    </div>
                    <div className="alert-time">Valid until 6:00 PM</div>
                  </div>
                </div>
                <div className="alert-item info">
                  <div className="alert-icon">🌡️</div>
                  <div className="alert-content">
                    <div className="alert-title">Temperature Drop</div>
                    <div className="alert-description">
                      Significant temperature decrease expected overnight
                    </div>
                    <div className="alert-time">Starts at 8:00 PM</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Overlay for mobile sidebar */}
      {sidebarOpen && (
        <div 
          className="sidebar-overlay"
          onClick={() => setSidebarOpen(false)}
        ></div>
      )}
    </div>
  );
};

export default ModernDashboard;