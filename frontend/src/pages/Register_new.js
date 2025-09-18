import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import './Register.css';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [formStep, setFormStep] = useState(1);
  const [passwordValidation, setPasswordValidation] = useState({
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    special: false,
    match: false
  });
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Password validation
    if (name === 'password') {
      validatePassword(value);
    }

    if (name === 'confirmPassword' || name === 'password') {
      const passwordToCheck = name === 'password' ? value : formData.password;
      const confirmToCheck = name === 'confirmPassword' ? value : formData.confirmPassword;
      setPasswordValidation(prev => ({
        ...prev,
        match: passwordToCheck === confirmToCheck && passwordToCheck !== ''
      }));
    }

    // Clear error when user starts typing
    if (error) {
      setError('');
    }
  };

  const validatePassword = (password) => {
    setPasswordValidation(prev => ({
      ...prev,
      length: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /\d/.test(password),
      special: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)
    }));
  };

  const isStep1Valid = () => {
    return formData.firstName && formData.lastName && formData.email;
  };

  const isStep2Valid = () => {
    return formData.username &&
      passwordValidation.length &&
      passwordValidation.uppercase &&
      passwordValidation.lowercase &&
      passwordValidation.number &&
      passwordValidation.special &&
      passwordValidation.match;
  };

  const handleNext = () => {
    if (formStep === 1 && isStep1Valid()) {
      setFormStep(2);
    }
  };

  const handleBack = () => {
    if (formStep === 2) {
      setFormStep(1);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!isStep2Valid()) {
      setError('Please complete all required fields with valid information');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await authService.register(formData);
      if (response.token) {
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));
        navigate('/dashboard');
      }
    } catch (err) {
      console.error('Registration error:', err);
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const togglePasswordVisibility = (field) => {
    if (field === 'password') {
      setShowPassword(!showPassword);
    } else {
      setShowConfirmPassword(!showConfirmPassword);
    }
  };

  const getPasswordStrength = () => {
    const validations = Object.values(passwordValidation);
    const validCount = validations.filter(v => v).length;

    if (validCount <= 2) return { level: 'weak', color: 'var(--red-500)' };
    if (validCount <= 4) return { level: 'medium', color: 'var(--yellow-500)' };
    return { level: 'strong', color: 'var(--green-500)' };
  };

  return (
    <div className="register-page">
      {/* Background Elements */}
      <div className="register-background">
        <div className="gradient-orb orb-1"></div>
        <div className="gradient-orb orb-2"></div>
        <div className="gradient-orb orb-3"></div>
      </div>

      {/* Navigation */}
      <nav className="register-nav">
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
      <div className="register-container">
        <div className="register-content">
          {/* Left Side - Welcome Content */}
          <div className="register-welcome">
            <div className="welcome-content">
              <div className="welcome-badge">
                <span className="badge-icon">🚀</span>
                <span>Get Started</span>
              </div>
              <h1 className="welcome-title">
                Join thousands of professionals using
                <span className="title-highlight"> WeatherVision</span>
              </h1>
              <p className="welcome-subtitle">
                Create your account and unlock advanced weather intelligence tools trusted by meteorologists worldwide.
              </p>

              <div className="stats-grid">
                <div className="stat-card">
                  <div className="stat-number">150K+</div>
                  <div className="stat-label">Active Users</div>
                </div>
                <div className="stat-card">
                  <div className="stat-number">98%</div>
                  <div className="stat-label">Accuracy Rate</div>
                </div>
                <div className="stat-card">
                  <div className="stat-number">195+</div>
                  <div className="stat-label">Countries</div>
                </div>
                <div className="stat-card">
                  <div className="stat-number">24/7</div>
                  <div className="stat-label">Support</div>
                </div>
              </div>

              <div className="benefits-list">
                <div className="benefit-item">
                  <span className="benefit-icon">✅</span>
                  <span>Free 14-day trial with full access</span>
                </div>
                <div className="benefit-item">
                  <span className="benefit-icon">✅</span>
                  <span>No credit card required</span>
                </div>
                <div className="benefit-item">
                  <span className="benefit-icon">✅</span>
                  <span>Cancel anytime, no questions asked</span>
                </div>
              </div>
            </div>
          </div>

          {/* Right Side - Register Form */}
          <div className="register-form-section">
            <div className="form-container">
              {/* Progress Indicator */}
              <div className="progress-indicator">
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{ width: `${(formStep / 2) * 100}%` }}
                  ></div>
                </div>
                <div className="progress-labels">
                  <span className={`progress-label ${formStep >= 1 ? 'active' : ''}`}>
                    Personal Info
                  </span>
                  <span className={`progress-label ${formStep >= 2 ? 'active' : ''}`}>
                    Account Setup
                  </span>
                </div>
              </div>

              <div className="form-header">
                <h2 className="form-title">
                  {formStep === 1 ? 'Personal Information' : 'Account Setup'}
                </h2>
                <p className="form-subtitle">
                  {formStep === 1
                    ? 'Tell us a bit about yourself'
                    : 'Create your secure account credentials'
                  }
                </p>
              </div>

              <form onSubmit={handleSubmit} className="register-form">
                {error && (
                  <div className="error-message">
                    <span className="error-icon">⚠️</span>
                    <span>{error}</span>
                  </div>
                )}

                {formStep === 1 ? (
                  <>
                    <div className="form-row">
                      <div className="form-group">
                        <label htmlFor="firstName" className="form-label">First Name</label>
                        <div className="input-wrapper">
                          <input
                            type="text"
                            id="firstName"
                            name="firstName"
                            value={formData.firstName}
                            onChange={handleChange}
                            className="form-input"
                            placeholder="Enter your first name"
                            required
                          />
                        </div>
                      </div>

                      <div className="form-group">
                        <label htmlFor="lastName" className="form-label">Last Name</label>
                        <div className="input-wrapper">
                          <input
                            type="text"
                            id="lastName"
                            name="lastName"
                            value={formData.lastName}
                            onChange={handleChange}
                            className="form-input"
                            placeholder="Enter your last name"
                            required
                          />
                        </div>
                      </div>
                    </div>

                    <div className="form-group">
                      <label htmlFor="email" className="form-label">Email Address</label>
                      <div className="input-wrapper">
                        <input
                          type="email"
                          id="email"
                          name="email"
                          value={formData.email}
                          onChange={handleChange}
                          className="form-input"
                          placeholder="Enter your email address"
                          required
                        />
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={handleNext}
                      disabled={!isStep1Valid()}
                      className="next-button"
                    >
                      <span>Continue</span>
                      <span className="button-icon">→</span>
                    </button>
                  </>
                ) : (
                  <>
                    <div className="form-group">
                      <label htmlFor="username" className="form-label">Username</label>
                      <div className="input-wrapper">
                        <input
                          type="text"
                          id="username"
                          name="username"
                          value={formData.username}
                          onChange={handleChange}
                          className="form-input"
                          placeholder="Choose a username"
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
                          placeholder="Create a secure password"
                          required
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword(!showPassword)}
                          className="password-toggle"
                        >
                          {showPassword ? "Hide" : "Show"}
                        </button>
                      </div>

                      {formData.password && (
                        <div className="password-strength">
                          <div className="strength-bar">
                            <div
                              className="strength-fill"
                              style={{
                                width: `${(Object.values(passwordValidation).filter(v => v).length / 6) * 100}%`,
                                background: getPasswordStrength().color
                              }}
                            ></div>
                          </div>
                          <span className="strength-label" style={{ color: getPasswordStrength().color }}>
                            {getPasswordStrength().level.toUpperCase()} PASSWORD
                          </span>
                        </div>
                      )}

                      <div className="password-requirements">
                        {[
                          { key: 'length', text: '8+ characters' },
                          { key: 'uppercase', text: 'Uppercase letter' },
                          { key: 'lowercase', text: 'Lowercase letter' },
                          { key: 'number', text: 'Number' },
                          { key: 'special', text: 'Special character' }
                        ].map(req => (
                          <div
                            key={req.key}
                            className={`requirement ${passwordValidation[req.key] ? 'met' : ''}`}
                          >
                            <span className="requirement-icon">
                              {passwordValidation[req.key] ? '✅' : '⭕'}
                            </span>
                            <span>{req.text}</span>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div className="form-group">
                      <label htmlFor="confirmPassword" className="form-label">Confirm Password</label>
                      <div className="input-wrapper">
                        <input
                          type={showConfirmPassword ? "text" : "password"}
                          id="confirmPassword"
                          name="confirmPassword"
                          value={formData.confirmPassword}
                          onChange={handleChange}
                          className="form-input"
                          placeholder="Confirm your password"
                          required
                        />
                        <button
                          type="button"
                          onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                          className="password-toggle"
                        >
                          {showConfirmPassword ? "Hide" : "Show"}
                        </button>
                      </div>
                      {formData.confirmPassword && (
                        <div className={`password-match ${passwordValidation.match ? 'match' : 'no-match'}`}>
                          <span className="match-icon">
                            {passwordValidation.match ? '✅' : '❌'}
                          </span>
                          <span>
                            {passwordValidation.match ? 'Passwords match' : 'Passwords do not match'}
                          </span>
                        </div>
                      )}
                    </div>

                    <div className="form-actions">
                      <button
                        type="button"
                        onClick={handleBack}
                        className="back-button"
                      >
                        <span className="button-icon">←</span>
                        <span>Back</span>
                      </button>

                      <button
                        type="submit"
                        disabled={loading || !isStep2Valid()}
                        className={`submit-button ${loading ? 'loading' : ''}`}
                      >
                        {loading ? (
                          <>
                            <span className="loading-spinner"></span>
                            Creating Account...
                          </>
                        ) : (
                          <>
                            <span>Create Account</span>
                            <span className="button-icon">🚀</span>
                          </>
                        )}
                      </button>
                    </div>
                  </>
                )}

                <div className="form-footer">
                  <p className="terms-text">
                    By creating an account, you agree to our{' '}
                    <Link to="/terms" className="terms-link">Terms of Service</Link>
                    {' '}and{' '}
                    <Link to="/privacy" className="terms-link">Privacy Policy</Link>
                  </p>

                  <p className="login-prompt">
                    Already have an account?{' '}
                    <Link to="/login" className="login-link">
                      Sign in here →
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

export default Register;
