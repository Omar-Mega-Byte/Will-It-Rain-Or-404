import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import weatherService from '../services/weatherService';
import './Home_new.css';

const Home = () => {
  const [currentTestimonial, setCurrentTestimonial] = useState(0);
  const [animatedStats, setAnimatedStats] = useState({
    users: 0,
    forecasts: 0,
    accuracy: 0,
    countries: 0
  });
  const [weatherData, setWeatherData] = useState(null);
  const [isLoadingWeather, setIsLoadingWeather] = useState(true);

  // Testimonials carousel
  const testimonials = [
    {
      name: 'Dr. Sarah Chen',
      role: 'Chief Meteorologist',
      company: 'National Weather Service',
      quote: 'WeatherVision has revolutionized our forecasting capabilities. The AI accuracy is phenomenal.',
      avatar: '👩‍🔬',
      rating: 5
    },
    {
      name: 'Mark Rodriguez',
      role: 'Aviation Director',
      company: 'SkyLine Airlines',
      quote: 'Critical for flight safety. WeatherVision provides the most reliable weather data available.',
      avatar: '👨‍✈️',
      rating: 5
    },
    {
      name: 'Emily Watson',
      role: 'Agricultural Scientist',
      company: 'GreenField Research',
      quote: 'The precision agriculture features have transformed our crop management strategies.',
      avatar: '👩‍🌾',
      rating: 5
    }
  ];

  // Fetch real weather data on component mount
  useEffect(() => {
    const fetchWeatherData = async () => {
      try {
        setIsLoadingWeather(true);
        const data = await weatherService.getRandomWeather();
        setWeatherData(data);
      } catch (error) {
        console.error('Error fetching weather data:', error);
        // Set fallback data if API fails
        setWeatherData({
          location: {
            name: "New York",
            country: "US"
          },
          current: {
            temperature: "22°C",
            condition: "Partly Cloudy",
            humidity: "65%",
            pressure: "1013 hPa",
            windSpeed: "12 km/h",
            visibility: "10 km"
          }
        });
      } finally {
        setIsLoadingWeather(false);
      }
    };

    fetchWeatherData();

    // Refresh weather data every 5 minutes
    const weatherInterval = setInterval(fetchWeatherData, 5 * 60 * 1000);

    return () => clearInterval(weatherInterval);
  }, []);

  // Auto-rotate testimonials
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentTestimonial((prev) => (prev + 1) % testimonials.length);
    }, 4000);
    return () => clearInterval(interval);
  }, [testimonials.length]);

  // Animate statistics on mount
  useEffect(() => {
    const animateValue = (start, end, duration, setter) => {
      let startTimestamp = null;
      const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        setter(Math.floor(progress * (end - start) + start));
        if (progress < 1) {
          window.requestAnimationFrame(step);
        }
      };
      window.requestAnimationFrame(step);
    };

    setTimeout(() => {
      animateValue(0, 150, 2000, (val) => setAnimatedStats(prev => ({ ...prev, users: val })));
      animateValue(0, 2500, 2500, (val) => setAnimatedStats(prev => ({ ...prev, forecasts: val })));
      animateValue(0, 98, 2200, (val) => setAnimatedStats(prev => ({ ...prev, accuracy: val })));
      animateValue(0, 195, 1800, (val) => setAnimatedStats(prev => ({ ...prev, countries: val })));
    }, 500);
  }, []);

  // Helper function to get weather icon based on condition
  const getWeatherIcon = (condition) => {
    const conditionLower = condition?.toLowerCase() || '';
    if (conditionLower.includes('clear') || conditionLower.includes('sunny')) return '☀️';
    if (conditionLower.includes('cloud')) return '⛅';
    if (conditionLower.includes('rain')) return '🌧️';
    if (conditionLower.includes('snow')) return '❄️';
    if (conditionLower.includes('storm')) return '⛈️';
    if (conditionLower.includes('fog') || conditionLower.includes('mist')) return '🌫️';
    return '☀️'; // default
  };

  // Helper function to format temperature
  const formatTemperature = (temp) => {
    if (typeof temp === 'string') return temp;
    if (typeof temp === 'number') return `${Math.round(temp)}°C`;
    return '22°C';
  };

  // Helper function to format wind speed
  const formatWindSpeed = (speed) => {
    if (typeof speed === 'string') return speed;
    if (typeof speed === 'number') return `${Math.round(speed)} km/h`;
    return '12 km/h';
  };

  // Helper function to format pressure
  const formatPressure = (pressure) => {
    if (typeof pressure === 'string') return pressure;
    if (typeof pressure === 'number') return `${Math.round(pressure)} hPa`;
    return '1013 hPa';
  };

  return (
    <div className="home-page">
      {/* Navigation */}
      <nav className="nav">
        <div className="nav-container">
          <div className="nav-brand">
            <Link to="/" className="brand-logo">
              <span className="logo-icon">🌦️</span>
              <span className="brand-text">WeatherVision</span>
            </Link>
          </div>
          <div className="nav-menu">
            <Link to="/features" className="nav-item">Features</Link>
            <Link to="/pricing" className="nav-item">Pricing</Link>
            <Link to="/about" className="nav-item">About</Link>
            <Link to="/login" className="nav-item nav-signin">Sign In</Link>
            <Link to="/register" className="nav-cta">Get Started</Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="hero">
        <div className="hero-container">
          <div className="hero-content">
            <div className="hero-badge">
              <span className="badge-icon">✨</span>
              <span>Powered by NASA Earth Data & Advanced AI</span>
            </div>
            <h1 className="hero-title">
              Weather Intelligence
              <span className="title-highlight"> Redefined</span>
            </h1>
            <p className="hero-subtitle">
              Experience the future of weather forecasting with AI-powered predictions,
              real-time satellite data, and precision that redefines accuracy.
            </p>
            <div className="hero-actions">
              <Link to="/register" className="btn btn-primary">
                <span>Start Free Trial</span>
                <span className="btn-icon">→</span>
              </Link>
              <Link to="/demo" className="btn btn-secondary">
                <span className="btn-icon">▶</span>
                <span>Watch Demo</span>
              </Link>
            </div>
            <div className="hero-stats">
              <div className="stat">
                <div className="stat-number">{animatedStats.users}K+</div>
                <div className="stat-label">Active Users</div>
              </div>
              <div className="stat">
                <div className="stat-number">{animatedStats.forecasts}K+</div>
                <div className="stat-label">Daily Predictions</div>
              </div>
              <div className="stat">
                <div className="stat-number">{animatedStats.accuracy}%</div>
                <div className="stat-label">Accuracy Rate</div>
              </div>
              <div className="stat">
                <div className="stat-number">{animatedStats.countries}+</div>
                <div className="stat-label">Countries</div>
              </div>
            </div>
          </div>

          <div className="hero-visual">
            <div className="weather-dashboard">
              <div className="dashboard-header">
                <div className="header-left">
                  <span className="location-icon">📍</span>
                  <span>
                    {isLoadingWeather
                      ? 'Loading...'
                      : `${weatherData?.location?.name || 'Unknown'}, ${weatherData?.location?.country || 'Unknown'}`
                    }
                  </span>
                </div>
                <div className="header-right">{new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</div>
              </div>

              <div className="weather-main">
                <div className="weather-icon-container">
                  <div className="weather-effects"></div>
                  <div className="weather-icon">
                    {isLoadingWeather ? '🔄' : getWeatherIcon(weatherData?.current?.condition)}
                  </div>
                </div>
                <div className="weather-info">
                  <div className="temperature">
                    {isLoadingWeather ? '--°C' : formatTemperature(weatherData?.current?.temperature)}
                  </div>
                  <div className="condition">
                    {isLoadingWeather ? 'Loading...' : (weatherData?.current?.condition || 'Unknown')}
                  </div>
                  <div className="feels-like">
                    Feels like {isLoadingWeather ? '--°C' : formatTemperature(weatherData?.current?.feelsLike || weatherData?.current?.temperature)}
                  </div>
                </div>
              </div>

              <div className="weather-details-grid">
                <div className="detail-item">
                  <div className="detail-icon">💨</div>
                  <div className="detail-content">
                    <div className="detail-value">
                      {isLoadingWeather ? '--' : formatWindSpeed(weatherData?.current?.windSpeed)}
                    </div>
                    <div className="detail-label">Wind</div>
                  </div>
                </div>
                <div className="detail-item">
                  <div className="detail-icon">💧</div>
                  <div className="detail-content">
                    <div className="detail-value">
                      {isLoadingWeather ? '--' : (weatherData?.current?.humidity || '65%')}
                    </div>
                    <div className="detail-label">Humidity</div>
                  </div>
                </div>
                <div className="detail-item">
                  <div className="detail-icon">👁️</div>
                  <div className="detail-content">
                    <div className="detail-value">
                      {isLoadingWeather ? '--' : (weatherData?.current?.visibility || '10 km')}
                    </div>
                    <div className="detail-label">Visibility</div>
                  </div>
                </div>
                <div className="detail-item">
                  <div className="detail-icon">📊</div>
                  <div className="detail-content">
                    <div className="detail-value">
                      {isLoadingWeather ? '--' : formatPressure(weatherData?.current?.pressure)}
                    </div>
                    <div className="detail-label">Pressure</div>
                  </div>
                </div>
              </div>

              <div className="forecast-hourly">
                <div className="hourly-item">
                  <div className="hourly-time">12 PM</div>
                  <div className="hourly-icon">{getWeatherIcon(weatherData?.current?.condition)}</div>
                  <div className="hourly-temp">{formatTemperature(weatherData?.current?.temperature)}</div>
                </div>
                <div className="hourly-item">
                  <div className="hourly-time">1 PM</div>
                  <div className="hourly-icon">{getWeatherIcon(weatherData?.current?.condition)}</div>
                  <div className="hourly-temp">{formatTemperature(weatherData?.current?.temperature)}</div>
                </div>
                <div className="hourly-item active">
                  <div className="hourly-time">2 PM</div>
                  <div className="hourly-icon">{getWeatherIcon(weatherData?.current?.condition)}</div>
                  <div className="hourly-temp">{formatTemperature(weatherData?.current?.temperature)}</div>
                </div>
                <div className="hourly-item">
                  <div className="hourly-time">3 PM</div>
                  <div className="hourly-icon">⛅</div>
                  <div className="hourly-temp">{formatTemperature((weatherData?.current?.temperature || 22) - 2)}</div>
                </div>
                <div className="hourly-item">
                  <div className="hourly-time">4 PM</div>
                  <div className="hourly-icon">🌧️</div>
                  <div className="hourly-temp">{formatTemperature((weatherData?.current?.temperature || 22) - 3)}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="features">
        <div className="container">
          <div className="section-header">
            <h2 className="section-title">Why Choose WeatherVision?</h2>
            <p className="section-subtitle">
              Advanced technology meets intuitive design for unparalleled weather intelligence
            </p>
          </div>

          <div className="features-grid">
            <div className="feature-card">
              <span className="feature-icon">🛰️</span>
              <h3 className="feature-title">NASA Satellite Data</h3>
              <p className="feature-description">
                Comprehensive weather data for 195+ countries with hyper-local forecasting capabilities.
              </p>
            </div>

            <div className="feature-card">
              <span className="feature-icon">🧠</span>
              <h3 className="feature-title">AI-Powered Predictions</h3>
              <p className="feature-description">
                Detailed insights, trends, and patterns with historical data going back 30+ years.
              </p>
            </div>

            <div className="feature-card">
              <span className="feature-icon">🔒</span>
              <h3 className="feature-title">Enterprise Security</h3>
              <p className="feature-description">
                Bank-grade security with SOC 2 compliance and 99.9% uptime guarantee.
              </p>
            </div>

            <div className="feature-card">
              <span className="feature-icon">⚡</span>
              <h3 className="feature-title">Real-Time Updates</h3>
              <p className="feature-description">
                Live weather tracking with minute-by-minute updates and instant alerts.
              </p>
            </div>

            <div className="feature-card">
              <span className="feature-icon">📊</span>
              <h3 className="feature-title">Advanced Analytics</h3>
              <p className="feature-description">
                Comprehensive reporting and data visualization tools for informed decisions.
              </p>
            </div>

            <div className="feature-card">
              <span className="feature-icon">🌍</span>
              <h3 className="feature-title">Global Coverage</h3>
              <p className="feature-description">
                Worldwide weather data with precision down to street-level accuracy.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials Section */}
      <section className="testimonials">
        <div className="container">
          <div className="testimonials-content">
            <div className="section-header">
              <h2 className="section-title">Trusted by Professionals</h2>
              <p className="section-subtitle">See what industry experts say about WeatherVision</p>
            </div>

            <div className="testimonial-card">
              <div className="testimonial-content">
                <div className="testimonial-stars">
                  {[...Array(testimonials[currentTestimonial].rating)].map((_, i) => (
                    <span key={i} className="star">⭐</span>
                  ))}
                </div>
                <blockquote className="testimonial-quote">
                  "{testimonials[currentTestimonial].quote}"
                </blockquote>
              </div>

              <div className="testimonial-author">
                <div className="author-avatar">
                  {testimonials[currentTestimonial].avatar}
                </div>
                <div className="author-info">
                  <div className="author-name">{testimonials[currentTestimonial].name}</div>
                  <div className="author-role">
                    {testimonials[currentTestimonial].role}, {testimonials[currentTestimonial].company}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta">
        <div className="container">
          <div className="cta-content">
            <h2 className="cta-title">
              Ready to Transform Your Weather Intelligence?
            </h2>
            <p className="cta-subtitle">
              Join thousands of professionals who trust WeatherVision for critical weather decisions.
            </p>
            <div className="cta-actions">
              <Link to="/register" className="btn btn-primary">
                <span>Start Free Trial</span>
                <span className="btn-icon">→</span>
              </Link>
              <Link to="/contact" className="btn btn-outline">
                <span>Contact Sales</span>
              </Link>
            </div>
            <div className="cta-features">
              <div className="cta-feature">
                <span className="feature-check">✓</span>
                <span>14-day free trial</span>
              </div>
              <div className="cta-feature">
                <span className="feature-check">✓</span>
                <span>No credit card required</span>
              </div>
              <div className="cta-feature">
                <span className="feature-check">✓</span>
                <span>Cancel anytime</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-brand">
              <div className="brand-logo">
                <span className="logo-icon">🌦️</span>
                <span className="brand-text">WeatherVision</span>
              </div>
              <p className="footer-description">
                Advanced weather intelligence powered by NASA data and artificial intelligence.
              </p>
            </div>

            <div className="link-group">
              <h4 className="link-title">Product</h4>
              <Link to="/features" className="link-item">Features</Link>
              <Link to="/pricing" className="link-item">Pricing</Link>
              <Link to="/api" className="link-item">API</Link>
              <Link to="/mobile" className="link-item">Mobile Apps</Link>
            </div>

            <div className="link-group">
              <h4 className="link-title">Company</h4>
              <Link to="/about" className="link-item">About</Link>
              <Link to="/careers" className="link-item">Careers</Link>
              <Link to="/press" className="link-item">Press</Link>
              <Link to="/contact" className="link-item">Contact</Link>
            </div>

            <div className="link-group">
              <h4 className="link-title">Resources</h4>
              <Link to="/docs" className="link-item">Documentation</Link>
              <Link to="/help" className="link-item">Help Center</Link>
              <Link to="/status" className="link-item">Status</Link>
              <Link to="/blog" className="link-item">Blog</Link>
            </div>
          </div>

          <div className="footer-bottom">
            <div className="footer-bottom-content">
              <p className="copyright">© 2024 WeatherVision. All rights reserved.</p>
              <div className="legal-links">
                <Link to="/privacy" className="link-item">Privacy</Link>
                <Link to="/terms" className="link-item">Terms</Link>
                <Link to="/cookies" className="link-item">Cookies</Link>
              </div>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Home;
