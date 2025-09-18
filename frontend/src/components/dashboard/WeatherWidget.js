import React, { useState, useEffect } from 'react';
import './WeatherWidget.css';

const WeatherWidget = () => {
  const [weather, setWeather] = useState({
    location: 'New York, NY',
    temperature: 22,
    condition: 'Partly Cloudy',
    humidity: 68,
    windSpeed: 15,
    pressure: 1013,
    uvIndex: 5,
    visibility: 10,
    feelsLike: 25,
    icon: '⛅'
  });

  const [forecast, setForecast] = useState([
    { day: 'Today', high: 25, low: 18, icon: '⛅', condition: 'Partly Cloudy' },
    { day: 'Tomorrow', high: 28, low: 20, icon: '☀️', condition: 'Sunny' },
    { day: 'Wed', high: 23, low: 16, icon: '🌧️', condition: 'Rainy' },
    { day: 'Thu', high: 26, low: 19, icon: '☀️', condition: 'Sunny' },
    { day: 'Fri', high: 24, low: 17, icon: '⛅', condition: 'Cloudy' }
  ]);

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Simulate weather data fetching
    fetchWeatherData();
  }, []);

  const fetchWeatherData = async () => {
    setLoading(true);
    // Simulate API delay
    setTimeout(() => {
      setLoading(false);
    }, 1000);
  };

  const getTemperatureColor = (temp) => {
    if (temp >= 30) return '#ff6b6b';
    if (temp >= 20) return '#ffa726';
    if (temp >= 10) return '#42a5f5';
    return '#66bb6a';
  };

  const getUVIndexColor = (uv) => {
    if (uv >= 8) return '#ff5722';
    if (uv >= 6) return '#ff9800';
    if (uv >= 3) return '#ffc107';
    return '#4caf50';
  };

  const getUVIndexLabel = (uv) => {
    if (uv >= 8) return 'Very High';
    if (uv >= 6) return 'High';
    if (uv >= 3) return 'Moderate';
    return 'Low';
  };

  if (loading) {
    return (
      <div className="weather-widget">
        <div className="widget-loading">
          <div className="loading-spinner"></div>
          <p>Loading weather data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="weather-widget">
      <div className="widget-header">
        <h3>🌤️ Current Weather</h3>
        <button className="refresh-btn" onClick={fetchWeatherData}>
          🔄
        </button>
      </div>

      {/* Main Weather Display */}
      <div className="current-weather">
        <div className="weather-main">
          <div className="weather-icon">{weather.icon}</div>
          <div className="weather-info">
            <div 
              className="temperature"
              style={{ color: getTemperatureColor(weather.temperature) }}
            >
              {weather.temperature}°C
            </div>
            <div className="condition">{weather.condition}</div>
            <div className="location">📍 {weather.location}</div>
          </div>
        </div>

        <div className="feels-like">
          Feels like {weather.feelsLike}°C
        </div>
      </div>

      {/* Weather Details */}
      <div className="weather-details">
        <div className="detail-item">
          <div className="detail-icon">💧</div>
          <div className="detail-info">
            <div className="detail-label">Humidity</div>
            <div className="detail-value">{weather.humidity}%</div>
          </div>
        </div>

        <div className="detail-item">
          <div className="detail-icon">💨</div>
          <div className="detail-info">
            <div className="detail-label">Wind Speed</div>
            <div className="detail-value">{weather.windSpeed} km/h</div>
          </div>
        </div>

        <div className="detail-item">
          <div className="detail-icon">🌡️</div>
          <div className="detail-info">
            <div className="detail-label">Pressure</div>
            <div className="detail-value">{weather.pressure} hPa</div>
          </div>
        </div>

        <div className="detail-item">
          <div className="detail-icon">👁️</div>
          <div className="detail-info">
            <div className="detail-label">Visibility</div>
            <div className="detail-value">{weather.visibility} km</div>
          </div>
        </div>

        <div className="detail-item">
          <div className="detail-icon">☀️</div>
          <div className="detail-info">
            <div className="detail-label">UV Index</div>
            <div 
              className="detail-value"
              style={{ color: getUVIndexColor(weather.uvIndex) }}
            >
              {weather.uvIndex} ({getUVIndexLabel(weather.uvIndex)})
            </div>
          </div>
        </div>
      </div>

      {/* 5-Day Forecast */}
      <div className="forecast-section">
        <h4 className="forecast-title">5-Day Forecast</h4>
        <div className="forecast-list">
          {forecast.map((day, index) => (
            <div key={index} className="forecast-item">
              <div className="forecast-day">{day.day}</div>
              <div className="forecast-icon">{day.icon}</div>
              <div className="forecast-temps">
                <span className="high-temp">{day.high}°</span>
                <span className="low-temp">{day.low}°</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Weather Actions */}
      <div className="weather-actions">
        <button className="action-btn primary">
          📊 View Detailed Forecast
        </button>
        <button className="action-btn secondary">
          🚨 Set Weather Alerts
        </button>
      </div>
    </div>
  );
};

export default WeatherWidget;