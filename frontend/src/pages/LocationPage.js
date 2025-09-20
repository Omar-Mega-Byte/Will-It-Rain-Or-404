import React, { useState, useEffect } from 'react';
import LocationSearch from '../components/LocationSearch';
import LocationList from '../components/LocationList';
import LocationForm from '../components/LocationForm';
import locationService from '../services/locationService';
import './LocationPage.css';

const LocationPage = () => {
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingLocation, setEditingLocation] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0
  });

  // Load initial locations
  useEffect(() => {
    loadLocations();
  }, [pagination.page, pagination.size]);

  // Load locations with current filters
  const loadLocations = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        search: searchQuery,
        sort: 'name,asc'
      };

      const result = await locationService.getAllLocations(params);
      
      if (result.success) {
        // Handle array response instead of paginated response
        setLocations(result.data || []);
        setPagination(prev => ({
          ...prev,
          totalElements: result.data?.length || 0,
          totalPages: 1 // Since we're getting all data at once
        }));
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError('Failed to load locations');
      console.error('Error loading locations:', err);
    } finally {
      setLoading(false);
    }
  };

  // Handle search
  const handleSearch = (query) => {
    setSearchQuery(query);
    setPagination(prev => ({ ...prev, page: 0 })); // Reset to first page
    // Debounce the search
    const timeoutId = setTimeout(() => {
      loadLocations();
    }, 300);
    
    return () => clearTimeout(timeoutId);
  };

  // Handle pagination
  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  // Handle location selection
  const handleLocationSelect = (location) => {
    console.log('Selected location:', location);
    // You can add navigation or other actions here
  };

  // Handle location creation
  const handleCreateLocation = () => {
    setEditingLocation(null);
    setShowForm(true);
  };

  // Handle location editing
  const handleEditLocation = (location) => {
    setEditingLocation(location);
    setShowForm(true);
  };

  // Handle form submission
  const handleFormSubmit = async (locationData) => {
    setLoading(true);
    setError(null);

    try {
      let result;
      if (editingLocation) {
        result = await locationService.updateLocation(editingLocation.id, locationData);
      } else {
        result = await locationService.createLocation(locationData);
      }

      if (result.success) {
        setShowForm(false);
        setEditingLocation(null);
        await loadLocations(); // Reload the list
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError('Failed to save location');
      console.error('Error saving location:', err);
    } finally {
      setLoading(false);
    }
  };

  // Handle location deletion
  const handleDeleteLocation = async (locationId) => {
    if (!window.confirm('Are you sure you want to delete this location?')) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const result = await locationService.deleteLocation(locationId);
      
      if (result.success) {
        await loadLocations(); // Reload the list
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError('Failed to delete location');
      console.error('Error deleting location:', err);
    } finally {
      setLoading(false);
    }
  };

  // Handle form cancel
  const handleFormCancel = () => {
    setShowForm(false);
    setEditingLocation(null);
  };

  if (loading && locations.length === 0) {
    return <div className="location-page--loading">Loading...</div>;
  }

  return (
    <div className="location-page">
      <header className="location-page__header">
        <h1>Location Management</h1>
        <button 
          className="btn btn-primary"
          onClick={handleCreateLocation}
          disabled={loading}
        >
          Add New Location
        </button>
      </header>

      {error && (
        <div className="location-page__error">
          <div className="alert alert-error">
            {error}
            <button 
              className="btn btn-small btn-secondary"
              onClick={() => setError(null)}
            >
              Dismiss
            </button>
          </div>
        </div>
      )}

      <div className="location-page__content">
        <section className="location-page__search">
          <LocationSearch
            onSearch={handleSearch}
            placeholder="Search locations by name, city, or country..."
            initialValue={searchQuery}
          />
        </section>

        <section className="location-page__list">
          <LocationList
            locations={locations}
            loading={loading}
            pagination={pagination}
            onPageChange={handlePageChange}
            onLocationSelect={handleLocationSelect}
            onEditLocation={handleEditLocation}
            onDeleteLocation={handleDeleteLocation}
          />
        </section>
      </div>

      {showForm && (
        <div className="location-page__modal">
          <div className="modal-overlay" onClick={handleFormCancel}></div>
          <div className="modal-content">
            <header className="modal-header">
              <h2>{editingLocation ? 'Edit Location' : 'Add New Location'}</h2>
              <button 
                className="btn btn-icon btn-secondary"
                onClick={handleFormCancel}
                aria-label="Close modal"
              >
                ×
              </button>
            </header>
            <div className="modal-body">
              <LocationForm
                initialData={editingLocation}
                onSubmit={handleFormSubmit}
                onCancel={handleFormCancel}
                loading={loading}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LocationPage;