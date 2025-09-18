import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import './Login.css';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error when user starts typing
    if (error) {
      setError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.username || !formData.password) {
      setError('Please fill in all fields');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await authService.login(formData.username, formData.password);
      if (response.token) {
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));
        navigate('/dashboard');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="login-page">
      {/* Background Elements */}
      <div className="login-background">
        <div className="gradient-orb orb-1"></div>
        <div className="gradient-orb orb-2"></div>
        <div className="gradient-orb orb-3"></div>
        <div className="floating-shapes">
          <div className="shape shape-1"></div>
          <div className="shape shape-2"></div>
          <div className="shape shape-3"></div>
          <div className="shape shape-4"></div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="login-nav">
        <div className="nav-container">
          <Link to="/" className="nav-brand">
            <span className="brand-text">WeatherVision</span>
          </Link>
          <div className="nav-links">
            <Link to="/" className="nav-link">← Back to Home</Link>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="login-container">
        <div className="login-content">
          {/* Left Side - Welcome Content */}
          <div className="login-welcome">
            <div className="welcome-content">
              <div className="welcome-badge">
                <span className="badge-icon">✨</span>
                <span>Welcome Back</span>
              </div>
              <h1 className="welcome-title">
                Sign in to your
                <span className="title-highlight"> WeatherVision</span> account
              </h1>
              <p className="welcome-subtitle">
                Access your personalized weather dashboard, saved locations, and premium forecasting tools.
              </p>

              <div className="feature-highlights">
                <div className="highlight-item">
                  <span className="highlight-icon">🎯</span>
                  <div className="highlight-text">
                    <h3>Personalized Forecasts</h3>
                    <p>Get weather predictions tailored to your locations</p>
                  </div>
                </div>
                <div className="highlight-item">
                  <span className="highlight-icon">📊</span>
                  <div className="highlight-text">
                    <h3>Advanced Analytics</h3>
                    <p>Track weather patterns and historical data</p>
                  </div>
                </div>
                <div className="highlight-item">
                  <span className="highlight-icon">⚡</span>
                  <div className="highlight-text">
                    <h3>Real-Time Alerts</h3>
                    <p>Instant notifications for severe weather conditions</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Right Side - Login Form */}
          <div className="login-form-section">
            <div className="form-container">
              <div className="form-header">
                <h2 className="form-title">Welcome Back</h2>
                <p className="form-subtitle">Sign in to continue to your account</p>
              </div>

              <form onSubmit={handleSubmit} className="login-form">
                {error && (
                  <div className="error-message">
                    <span className="error-icon">⚠️</span>
                    <span>{error}</span>
                  </div>
                )}

                <div className="form-group">
                  <label htmlFor="username" className="form-label">Username or Email</label>
                  <div className="input-wrapper">
                    <input
                      type="text"
                      id="username"
                      name="username"
                      value={formData.username}
                      onChange={handleChange}
                      className="form-input"
                      placeholder="Enter your username or email"
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="password" className="form-label">Password</label>
                  <div className="input-wrapper">
                    <input
                      type={showPassword ? "text" : "password"}
                      id="password"
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      className="form-input"
                      placeholder="Enter your password"
                      required
                    />
                    <button
                      type="button"
                      onClick={togglePasswordVisibility}
                      className="password-toggle"
                    >
                      {showPassword ? "Hide" : "Show"}
                    </button>
                  </div>
                </div>

                <div className="form-options">
                  <label className="checkbox-wrapper">
                    <input type="checkbox" className="checkbox" />
                    <span className="checkmark"></span>
                    <span className="checkbox-label">Remember me</span>
                  </label>
                  <Link to="/forgot-password" className="forgot-link">Forgot password?</Link>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className={`submit-button ${loading ? 'loading' : ''}`}
                >
                  {loading ? (
                    <>
                      <span className="loading-spinner"></span>
                      Signing in...
                    </>
                  ) : (
                    <>
                      <span>Sign In</span>
                      <span className="button-icon">→</span>
                    </>
                  )}
                </button>

                <div className="divider">
                  <span className="divider-text">or continue with</span>
                </div>

                <div className="social-login">
                  <button type="button" className="social-button google">
                    <span className="social-icon">🔵</span>
                    <span>Google</span>
                  </button>
                  <button type="button" className="social-button github">
                    <span className="social-icon">⚫</span>
                    <span>GitHub</span>
                  </button>
                </div>

                <div className="form-footer">
                  <p className="signup-prompt">
                    Don't have an account?{' '}
                    <Link to="/register" className="signup-link">
                      Create one here →
                    </Link>
                  </p>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
