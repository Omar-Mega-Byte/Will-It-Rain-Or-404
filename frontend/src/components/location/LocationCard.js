import React, { useState, useEffect } from 'react';
import './LocationCard.css';

const LocationCard = ({ location, onEdit, onDelete, onView, isSelected, onSelect }) => {
  const [isLoading, setIsLoading] = useState(false);

  const handleEdit = () => {
    if (onEdit) {
      onEdit(location);
    }
  };

  const handleDelete = async () => {
    if (window.confirm(`Are you sure you want to delete "${location.name}"?`)) {
      setIsLoading(true);
      try {
        await onDelete(location.id);
      } catch (error) {
        console.error('Error deleting location:', error);
        alert('Failed to delete location. Please try again.');
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleView = () => {
    if (onView) {
      onView(location);
    }
  };

  const handleSelect = () => {
    if (onSelect) {
      onSelect(location);
    }
  };

  const formatCoordinates = (lat, lng) => {
    return `${parseFloat(lat).toFixed(4)}, ${parseFloat(lng).toFixed(4)}`;
  };

  const getLocationDisplayAddress = () => {
    const parts = [];
    if (location.city) parts.push(location.city);
    if (location.state) parts.push(location.state);
    if (location.country) parts.push(location.country);
    return parts.join(', ') || 'No address specified';
  };

  return (
    <div className={`location-card ${isSelected ? 'selected' : ''} ${isLoading ? 'loading' : ''}`}>
      <div className="location-card-header">
        <div className="location-info">
          <h3 className="location-name" onClick={handleView} title="Click to view details">
            {location.name}
          </h3>
          <p className="location-address">{getLocationDisplayAddress()}</p>
        </div>
        {onSelect && (
          <div className="location-select">
            <input
              type="checkbox"
              checked={isSelected}
              onChange={handleSelect}
              disabled={isLoading}
            />
          </div>
        )}
      </div>

      <div className="location-card-body">
        <div className="location-details">
          <div className="detail-row">
            <span className="detail-label">Coordinates:</span>
            <span className="detail-value">
              {formatCoordinates(location.latitude, location.longitude)}
            </span>
          </div>
          
          {location.timezone && (
            <div className="detail-row">
              <span className="detail-label">Timezone:</span>
              <span className="detail-value">{location.timezone}</span>
            </div>
          )}
          
          {location.elevation !== null && location.elevation !== undefined && (
            <div className="detail-row">
              <span className="detail-label">Elevation:</span>
              <span className="detail-value">{location.elevation}m</span>
            </div>
          )}
        </div>

        {location.address && (
          <div className="location-full-address">
            <span className="detail-label">Full Address:</span>
            <p className="address-text">{location.address}</p>
          </div>
        )}
      </div>

      <div className="location-card-footer">
        <div className="location-meta">
          <small className="created-date">
            Added: {new Date(location.createdAt).toLocaleDateString()}
          </small>
          {location.updatedAt !== location.createdAt && (
            <small className="updated-date">
              Updated: {new Date(location.updatedAt).toLocaleDateString()}
            </small>
          )}
        </div>

        <div className="location-actions">
          <button
            className="btn btn-secondary btn-sm"
            onClick={handleView}
            disabled={isLoading}
            title="View details"
          >
            View
          </button>
          
          {onEdit && (
            <button
              className="btn btn-primary btn-sm"
              onClick={handleEdit}
              disabled={isLoading}
              title="Edit location"
            >
              Edit
            </button>
          )}
          
          {onDelete && (
            <button
              className="btn btn-danger btn-sm"
              onClick={handleDelete}
              disabled={isLoading}
              title="Delete location"
            >
              {isLoading ? 'Deleting...' : 'Delete'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default LocationCard;