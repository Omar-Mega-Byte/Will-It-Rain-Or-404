import React from 'react';

const StatCard = ({ icon, number, label, description, className = '' }) => {
  return (
    <div className={`stat-card ${className}`}>
      <div className="stat-icon">{icon}</div>
      <div className="stat-number">{typeof number === 'number' ? number.toLocaleString() : number}</div>
      <div className="stat-label">{label}</div>
      {description && <div className="stat-description">{description}</div>}
    </div>
  );
};

export default StatCard;
