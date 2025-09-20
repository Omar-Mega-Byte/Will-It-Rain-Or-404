import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login_new';
import Register from './pages/Register_new';
import Dashboard from './pages/Dashboard';
import Events from './pages/Events';
import Calendar from './pages/Calendar';
import Analytics from './components/analytics/Analytics';
import './App.css';
import './styles/components/Common.css';
import './styles/components/EventCard.css';
import './styles/components/EventForm.css';
import './styles/components/EventModal.css';
import './styles/components/EventSearch.css';

// Simple Welcome component defined in App.js to avoid import issues
const Welcome = () => {
  return (
    <div className="welcome-container">
      <div className="welcome-content">
        <header className="welcome-header">
          <div className="nav-brand">
            <span className="brand-icon">🌦️</span>
            <h1 className="brand-name">WeatherVision</h1>
          </div>
          <nav className="nav-links">
            <Link to="/login" className="nav-link">
              <span className="nav-icon">🚀</span>
              Sign In
            </Link>
            <Link to="/register" className="nav-link nav-link-primary">
              <span className="nav-icon">⭐</span>
              Get Started
            </Link>
          </nav>
        </header>

        <main className="hero-section">
          <div className="hero-content">
            <div className="hero-badge">
              🆕 Now with Real-Time NASA Satellite Data
            </div>

            <h2 className="hero-title">
              Discover the Future of
              <span className="gradient-text"> Weather Intelligence</span>
            </h2>

            <p className="hero-subtitle">
              Experience weather forecasting like never before with our AI-powered platform
              that combines NASA Earth observation data with cutting-edge machine learning.
            </p>

            <div className="cta-section">
              <Link to="/register" className="cta-button cta-primary">
                <span className="cta-icon">🚀</span>
                Start Your Weather Journey
              </Link>

              <Link to="/home" className="cta-button cta-secondary">
                <span className="cta-icon">🔍</span>
                Explore Features
              </Link>
            </div>
          </div>
        </main>

        <footer className="welcome-footer">
          <p className="footer-text">
            ✨ Powered by NASA Earth Data & Advanced AI ✨
          </p>
        </footer>
      </div>
    </div>
  );
};

// Landing page component
const Landing = () => {
  return <Welcome />;
};

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          {/* Public routes */}
          <Route path="/" element={<Home />} />
          <Route path="/welcome" element={<Landing />} />
          <Route path="/home" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected routes */}
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/events" element={<Events />} />
          <Route path="/calendar" element={<Calendar />} />
          <Route path="/analytics/*" element={<Analytics />} />

          {/* Catch all route */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
