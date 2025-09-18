import React, { useState, useEffect } from 'react';
import './LocationForm.css';

const LocationForm = ({ 
  onSubmit, 
  onCancel, 
  initialData = null, 
  isLoading = false,
  title = null 
}) => {
  const [formData, setFormData] = useState({
    name: '',
    latitude: '',
    longitude: '',
    country: '',
    state: '',
    city: '',
    address: '',
    timezone: '',
    elevation: ''
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Populate form with initial data if editing
  useEffect(() => {
    if (initialData) {
      setFormData({
        name: initialData.name || '',
        latitude: initialData.latitude || '',
        longitude: initialData.longitude || '',
        country: initialData.country || '',
        state: initialData.state || '',
        city: initialData.city || '',
        address: initialData.address || '',
        timezone: initialData.timezone || '',
        elevation: initialData.elevation || ''
      });
    }
  }, [initialData]);

  // Form validation
  const validateForm = () => {
    const newErrors = {};

    // Required fields
    if (!formData.name.trim()) {
      newErrors.name = 'Location name is required';
    } else if (formData.name.length > 255) {
      newErrors.name = 'Location name must not exceed 255 characters';
    }

    if (!formData.latitude) {
      newErrors.latitude = 'Latitude is required';
    } else {
      const lat = parseFloat(formData.latitude);
      if (isNaN(lat) || lat < -90 || lat > 90) {
        newErrors.latitude = 'Latitude must be between -90 and 90';
      }
    }

    if (!formData.longitude) {
      newErrors.longitude = 'Longitude is required';
    } else {
      const lng = parseFloat(formData.longitude);
      if (isNaN(lng) || lng < -180 || lng > 180) {
        newErrors.longitude = 'Longitude must be between -180 and 180';
      }
    }

    // Optional field validations
    if (formData.country && formData.country.length > 100) {
      newErrors.country = 'Country name must not exceed 100 characters';
    }

    if (formData.state && formData.state.length > 100) {
      newErrors.state = 'State name must not exceed 100 characters';
    }

    if (formData.city && formData.city.length > 100) {
      newErrors.city = 'City name must not exceed 100 characters';
    }

    if (formData.address && formData.address.length > 1000) {
      newErrors.address = 'Address must not exceed 1000 characters';
    }

    if (formData.timezone && formData.timezone.length > 50) {
      newErrors.timezone = 'Timezone must not exceed 50 characters';
    }

    if (formData.elevation) {
      const elevation = parseInt(formData.elevation);
      if (isNaN(elevation) || elevation < -500 || elevation > 10000) {
        newErrors.elevation = 'Elevation must be between -500 and 10000 meters';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);
    
    try {
      // Prepare data for submission
      const submitData = {
        ...formData,
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
        elevation: formData.elevation ? parseInt(formData.elevation) : null
      };

      // Remove empty strings
      Object.keys(submitData).forEach(key => {
        if (submitData[key] === '') {
          submitData[key] = null;
        }
      });

      await onSubmit(submitData);
    } catch (error) {
      console.error('Form submission error:', error);
      // Handle submission error
      if (error.response && error.response.data && error.response.data.message) {
        setErrors({ submit: error.response.data.message });
      } else {
        setErrors({ submit: 'An error occurred while saving the location. Please try again.' });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    }
  };

  const getFormTitle = () => {
    if (title) return title;
    return initialData ? 'Edit Location' : 'Add New Location';
  };

  const getSubmitButtonText = () => {
    if (isSubmitting) return 'Saving...';
    return initialData ? 'Update Location' : 'Add Location';
  };

  return (
    <div className="location-form-container">
      <form className="location-form" onSubmit={handleSubmit}>
        <div className="form-header">
          <h2 className="form-title">{getFormTitle()}</h2>
        </div>

        {errors.submit && (
          <div className="error-message global-error">
            {errors.submit}
          </div>
        )}

        <div className="form-body">
          {/* Required Fields */}
          <div className="form-section">
            <h3 className="section-title">Required Information</h3>
            
            <div className="form-group">
              <label htmlFor="name" className="form-label required">
                Location Name
              </label>
              <input
                type="text"
                id="name"
                name="name"
                className={`form-input ${errors.name ? 'error' : ''}`}
                value={formData.name}
                onChange={handleInputChange}
                placeholder="Enter location name"
                disabled={isSubmitting || isLoading}
                maxLength={255}
              />
              {errors.name && <span className="error-message">{errors.name}</span>}
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="latitude" className="form-label required">
                  Latitude
                </label>
                <input
                  type="number"
                  id="latitude"
                  name="latitude"
                  className={`form-input ${errors.latitude ? 'error' : ''}`}
                  value={formData.latitude}
                  onChange={handleInputChange}
                  placeholder="e.g., 40.7128"
                  step="any"
                  min="-90"
                  max="90"
                  disabled={isSubmitting || isLoading}
                />
                {errors.latitude && <span className="error-message">{errors.latitude}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="longitude" className="form-label required">
                  Longitude
                </label>
                <input
                  type="number"
                  id="longitude"
                  name="longitude"
                  className={`form-input ${errors.longitude ? 'error' : ''}`}
                  value={formData.longitude}
                  onChange={handleInputChange}
                  placeholder="e.g., -74.0060"
                  step="any"
                  min="-180"
                  max="180"
                  disabled={isSubmitting || isLoading}
                />
                {errors.longitude && <span className="error-message">{errors.longitude}</span>}
              </div>
            </div>
          </div>

          {/* Location Details */}
          <div className="form-section">
            <h3 className="section-title">Location Details</h3>
            
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="country" className="form-label">
                  Country
                </label>
                <input
                  type="text"
                  id="country"
                  name="country"
                  className={`form-input ${errors.country ? 'error' : ''}`}
                  value={formData.country}
                  onChange={handleInputChange}
                  placeholder="e.g., United States"
                  disabled={isSubmitting || isLoading}
                  maxLength={100}
                />
                {errors.country && <span className="error-message">{errors.country}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="state" className="form-label">
                  State/Province
                </label>
                <input
                  type="text"
                  id="state"
                  name="state"
                  className={`form-input ${errors.state ? 'error' : ''}`}
                  value={formData.state}
                  onChange={handleInputChange}
                  placeholder="e.g., New York"
                  disabled={isSubmitting || isLoading}
                  maxLength={100}
                />
                {errors.state && <span className="error-message">{errors.state}</span>}
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="city" className="form-label">
                City
              </label>
              <input
                type="text"
                id="city"
                name="city"
                className={`form-input ${errors.city ? 'error' : ''}`}
                value={formData.city}
                onChange={handleInputChange}
                placeholder="e.g., New York City"
                disabled={isSubmitting || isLoading}
                maxLength={100}
              />
              {errors.city && <span className="error-message">{errors.city}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="address" className="form-label">
                Full Address
              </label>
              <textarea
                id="address"
                name="address"
                className={`form-input form-textarea ${errors.address ? 'error' : ''}`}
                value={formData.address}
                onChange={handleInputChange}
                placeholder="Enter the complete address"
                disabled={isSubmitting || isLoading}
                maxLength={1000}
                rows={3}
              />
              {errors.address && <span className="error-message">{errors.address}</span>}
            </div>
          </div>

          {/* Additional Information */}
          <div className="form-section">
            <h3 className="section-title">Additional Information</h3>
            
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="timezone" className="form-label">
                  Timezone
                </label>
                <input
                  type="text"
                  id="timezone"
                  name="timezone"
                  className={`form-input ${errors.timezone ? 'error' : ''}`}
                  value={formData.timezone}
                  onChange={handleInputChange}
                  placeholder="e.g., America/New_York"
                  disabled={isSubmitting || isLoading}
                  maxLength={50}
                />
                {errors.timezone && <span className="error-message">{errors.timezone}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="elevation" className="form-label">
                  Elevation (meters)
                </label>
                <input
                  type="number"
                  id="elevation"
                  name="elevation"
                  className={`form-input ${errors.elevation ? 'error' : ''}`}
                  value={formData.elevation}
                  onChange={handleInputChange}
                  placeholder="e.g., 10"
                  min="-500"
                  max="10000"
                  disabled={isSubmitting || isLoading}
                />
                {errors.elevation && <span className="error-message">{errors.elevation}</span>}
              </div>
            </div>
          </div>
        </div>

        <div className="form-footer">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={handleCancel}
            disabled={isSubmitting || isLoading}
          >
            Cancel
          </button>
          
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isSubmitting || isLoading}
          >
            {getSubmitButtonText()}
          </button>
        </div>
      </form>
    </div>
  );
};

export default LocationForm;