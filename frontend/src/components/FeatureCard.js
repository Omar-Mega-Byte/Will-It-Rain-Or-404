import React from 'react';

const FeatureCard = ({ icon, title, description, details, className = '' }) => {
  return (
    <div className={`feature-card ${className}`}>
      <div className="feature-icon">{icon}</div>
      <h3 className="feature-title">{title}</h3>
      <p className="feature-description">{description}</p>
      {details && <p className="feature-details">{details}</p>}
      <div className="feature-arrow">→</div>
    </div>
  );
};

export default FeatureCard;
