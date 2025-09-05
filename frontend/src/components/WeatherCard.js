import React from 'react';

const WeatherCard = ({ location, date, temperature, feelsLike, icon, details, forecast, className = '' }) => {
  return (
    <div className={`hero-card ${className}`}>
      <div className="weather-visualization">
        <div className="weather-header">
          <div className="location">📍 {location}</div>
          <div className="date">{date}</div>
        </div>
        <div className="weather-main">
          <div className="weather-icon">{icon}</div>
          <div className="weather-temp">
            <span className="temp-main">{temperature}</span>
            {feelsLike && <span className="temp-feels">Feels like {feelsLike}</span>}
          </div>
        </div>
        {details && (
          <div className="weather-details">
            {details.map((detail, index) => (
              <div key={index} className="weather-detail">
                <span className="detail-icon">{detail.icon}</span>
                <span>{detail.text}</span>
              </div>
            ))}
          </div>
        )}
        {forecast && (
          <div className="forecast-strip">
            {forecast.map((item, index) => (
              <div key={index} className="forecast-item">
                <div className="forecast-time">{item.time}</div>
                <div className="forecast-icon">{item.icon}</div>
                <div className="forecast-temp">{item.temp}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default WeatherCard;
