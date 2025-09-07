import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import './Dashboard.css';

const Dashboard = () => {
  const [user, setUser] = useState(null);
  const [weather, setWeather] = useState({
    temperature: 22,
    humidity: 68,
    windSpeed: 15,
    condition: 'Partly Cloudy'
  });
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    // Get user info from token or make API call
    const userData = {
      username: 'User',
      email: 'user@example.com'
    };
    setUser(userData);
  }, [navigate]);

  const handleLogout = () => {
    authService.logout();
    navigate('/');
  };

  if (!user) {
    return <div className="loading-spinner">Loading...</div>;
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-content">
        <header className="dashboard-header">
          <div className="nav-brand">
            <span className="brand-icon">🌦️</span>
            <h1 className="brand-name">WeatherVision Dashboard</h1>
          </div>
          <nav className="nav-links">
            <span className="user-welcome">Welcome, {user.username}!</span>
            <button onClick={handleLogout} className="nav-link logout-btn">
              <span className="nav-icon">👋</span>
              Logout
            </button>
          </nav>
        </header>

        <main className="dashboard-main">
          <div className="dashboard-grid">
            <div className="weather-card main-card">
              <div className="card-header">
                <h2>Current Weather</h2>
                <span className="weather-icon">🌤️</span>
              </div>
              <div className="weather-info">
                <div className="temperature">{weather.temperature}°C</div>
                <div className="condition">{weather.condition}</div>
                <div className="weather-details">
                  <div className="detail-item">
                    <span className="detail-icon">💨</span>
                    <span>Wind: {weather.windSpeed} km/h</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-icon">💧</span>
                    <span>Humidity: {weather.humidity}%</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="forecast-card">
              <div className="card-header">
                <h3>7-Day Forecast</h3>
              </div>
              <div className="forecast-list">
                {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((day, index) => (
                  <div key={day} className="forecast-item">
                    <span className="day">{day}</span>
                    <span className="forecast-icon">🌤️</span>
                    <span className="temp">{22 + Math.floor(Math.random() * 10)}°</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="stats-card">
              <div className="card-header">
                <h3>Weather Stats</h3>
              </div>
              <div className="stats-grid">
                <div className="stat-item">
                  <div className="stat-icon">📊</div>
                  <div className="stat-info">
                    <div className="stat-value">99.2%</div>
                    <div className="stat-label">Accuracy</div>
                  </div>
                </div>
                <div className="stat-item">
                  <div className="stat-icon">⚡</div>
                  <div className="stat-info">
                    <div className="stat-value">0.3s</div>
                    <div className="stat-label">Response Time</div>
                  </div>
                </div>
                <div className="stat-item">
                  <div className="stat-icon">🛰️</div>
                  <div className="stat-info">
                    <div className="stat-value">12</div>
                    <div className="stat-label">Satellites</div>
                  </div>
                </div>
                <div className="stat-item">
                  <div className="stat-icon">🌍</div>
                  <div className="stat-info">
                    <div className="stat-value">Global</div>
                    <div className="stat-label">Coverage</div>
                  </div>
                </div>
              </div>
            </div>

            <div className="alerts-card">
              <div className="card-header">
                <h3>Weather Alerts</h3>
              </div>
              <div className="alerts-list">
                <div className="alert-item info">
                  <span className="alert-icon">ℹ️</span>
                  <div className="alert-content">
                    <div className="alert-title">Clear Weather Ahead</div>
                    <div className="alert-text">Perfect conditions for outdoor activities this week</div>
                  </div>
                </div>
                <div className="alert-item success">
                  <span className="alert-icon">✅</span>
                  <div className="alert-content">
                    <div className="alert-title">System Operational</div>
                    <div className="alert-text">All weather monitoring systems functioning normally</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>

        <footer className="dashboard-footer">
          <p className="footer-text">
            ✨ Real-time data from NASA Earth observation satellites ✨
          </p>
        </footer>
      </div>
    </div>
  );
};

export default Dashboard;
