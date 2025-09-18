import React from 'react';
import './WeatherChart.css';

const WeatherChart = () => {
  const hourlyData = [
    { time: '6 AM', temp: 18, humidity: 65, icon: '🌅' },
    { time: '9 AM', temp: 22, humidity: 60, icon: '☀️' },
    { time: '12 PM', temp: 28, humidity: 55, icon: '☀️' },
    { time: '3 PM', temp: 31, humidity: 50, icon: '☀️' },
    { time: '6 PM', temp: 26, humidity: 58, icon: '🌤️' },
    { time: '9 PM', temp: 21, humidity: 68, icon: '🌙' },
  ];

  const maxTemp = Math.max(...hourlyData.map(d => d.temp));
  const minTemp = Math.min(...hourlyData.map(d => d.temp));

  const getTemperatureHeight = (temp) => {
    return ((temp - minTemp) / (maxTemp - minTemp)) * 100;
  };

  const getHumidityHeight = (humidity) => {
    return humidity;
  };

  return (
    <div className="weather-chart">
      <div className="widget-header">
        <h3>📈 24-Hour Weather Trend</h3>
        <div className="chart-legend">
          <span className="legend-item">
            <span className="legend-color temp-color"></span>
            Temperature
          </span>
          <span className="legend-item">
            <span className="legend-color humidity-color"></span>
            Humidity
          </span>
        </div>
      </div>

      <div className="chart-container">
        <div className="chart-grid">
          {hourlyData.map((data, index) => (
            <div key={index} className="chart-column">
              <div className="chart-bars">
                <div 
                  className="temp-bar"
                  style={{ height: `${getTemperatureHeight(data.temp)}%` }}
                  title={`${data.temp}°C`}
                >
                  <span className="bar-value">{data.temp}°</span>
                </div>
                <div 
                  className="humidity-bar"
                  style={{ height: `${getHumidityHeight(data.humidity)}%` }}
                  title={`${data.humidity}% humidity`}
                >
                  <span className="bar-value">{data.humidity}%</span>
                </div>
              </div>
              <div className="time-label">
                <div className="weather-icon">{data.icon}</div>
                <div className="time-text">{data.time}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="chart-summary">
        <div className="summary-item">
          <div className="summary-icon">🌡️</div>
          <div className="summary-content">
            <div className="summary-label">Temperature Range</div>
            <div className="summary-value">{minTemp}° - {maxTemp}°C</div>
          </div>
        </div>
        <div className="summary-item">
          <div className="summary-icon">💧</div>
          <div className="summary-content">
            <div className="summary-label">Avg Humidity</div>
            <div className="summary-value">
              {Math.round(hourlyData.reduce((sum, d) => sum + d.humidity, 0) / hourlyData.length)}%
            </div>
          </div>
        </div>
        <div className="summary-item">
          <div className="summary-icon">📊</div>
          <div className="summary-content">
            <div className="summary-label">Trend</div>
            <div className="summary-value">Rising</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default WeatherChart;