import React, { useState, useEffect } from 'react';
import LocationCard from './LocationCard';
import LocationSearch from './LocationSearch';
import Pagination from '../Pagination';
import LoadingSpinner from '../LoadingSpinner';
import './LocationList.css';

const LocationList = ({ 
  locations = [], 
  onLocationEdit, 
  onLocationDelete, 
  onLocationView,
  onLocationSelect,
  selectedLocationIds = [],
  showSearch = true,
  showPagination = true,
  onSearch,
  isLoading = false,
  error = null,
  emptyMessage = "No locations found",
  title = "Locations"
}) => {
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);
  const [filteredLocations, setFilteredLocations] = useState(locations);
  const [searchQuery, setSearchQuery] = useState('');

  // Update filtered locations when locations prop changes
  useEffect(() => {
    setFilteredLocations(locations);
  }, [locations]);

  // Handle local search if onSearch is not provided
  const handleSearch = async (query) => {
    setSearchQuery(query);
    setCurrentPage(1); // Reset to first page when searching
    
    if (onSearch) {
      // Use parent component's search function
      await onSearch(query);
    } else {
      // Perform local filtering
      if (!query.trim()) {
        setFilteredLocations(locations);
        return;
      }
      
      const filtered = locations.filter(location => 
        location.name.toLowerCase().includes(query.toLowerCase()) ||
        (location.city && location.city.toLowerCase().includes(query.toLowerCase())) ||
        (location.country && location.country.toLowerCase().includes(query.toLowerCase())) ||
        (location.state && location.state.toLowerCase().includes(query.toLowerCase()))
      );
      
      setFilteredLocations(filtered);
    }
  };

  const handleClearSearch = () => {
    setSearchQuery('');
    setCurrentPage(1);
    if (onSearch) {
      onSearch('');
    } else {
      setFilteredLocations(locations);
    }
  };

  // Calculate pagination
  const totalItems = filteredLocations.length;
  const totalPages = Math.ceil(totalItems / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = Math.min(startIndex + itemsPerPage, totalItems);
  const currentLocations = filteredLocations.slice(startIndex, endIndex);

  const handlePageChange = (page) => {
    setCurrentPage(page);
    // Scroll to top of list
    const listElement = document.querySelector('.location-list-container');
    if (listElement) {
      listElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  const isLocationSelected = (locationId) => {
    return selectedLocationIds.includes(locationId);
  };

  const handleLocationSelect = (location) => {
    if (onLocationSelect) {
      onLocationSelect(location);
    }
  };

  // Bulk actions
  const getSelectedCount = () => selectedLocationIds.length;

  const renderHeader = () => (
    <div className="location-list-header">
      <div className="header-title">
        <h2>{title}</h2>
        {totalItems > 0 && (
          <span className="item-count">
            {searchQuery ? `${totalItems} results` : `${totalItems} locations`}
          </span>
        )}
      </div>
      
      {getSelectedCount() > 0 && (
        <div className="bulk-actions">
          <span className="selected-count">
            {getSelectedCount()} selected
          </span>
          {/* Add bulk action buttons here if needed */}
        </div>
      )}
    </div>
  );

  const renderEmptyState = () => (
    <div className="empty-state">
      <div className="empty-state-icon">📍</div>
      <h3>No locations found</h3>
      <p>
        {searchQuery 
          ? `No locations match "${searchQuery}". Try a different search term.`
          : emptyMessage
        }
      </p>
      {searchQuery && (
        <button className="btn btn-primary" onClick={handleClearSearch}>
          Clear Search
        </button>
      )}
    </div>
  );

  const renderError = () => (
    <div className="error-state">
      <div className="error-icon">⚠️</div>
      <h3>Error Loading Locations</h3>
      <p>{error}</p>
      <button className="btn btn-primary" onClick={() => window.location.reload()}>
        Try Again
      </button>
    </div>
  );

  if (error) {
    return (
      <div className="location-list-container">
        {renderHeader()}
        {renderError()}
      </div>
    );
  }

  return (
    <div className="location-list-container">
      {renderHeader()}
      
      {showSearch && (
        <div className="search-section">
          <LocationSearch
            onSearch={handleSearch}
            onClear={handleClearSearch}
            initialQuery={searchQuery}
            placeholder="Search locations by name, city, country..."
          />
        </div>
      )}

      {isLoading ? (
        <div className="loading-section">
          <LoadingSpinner />
          <p>Loading locations...</p>
        </div>
      ) : (
        <>
          {currentLocations.length === 0 ? (
            renderEmptyState()
          ) : (
            <>
              <div className="location-list">
                {currentLocations.map((location) => (
                  <LocationCard
                    key={location.id}
                    location={location}
                    onEdit={onLocationEdit}
                    onDelete={onLocationDelete}
                    onView={onLocationView}
                    onSelect={onLocationSelect ? handleLocationSelect : null}
                    isSelected={isLocationSelected(location.id)}
                  />
                ))}
              </div>

              {showPagination && totalPages > 1 && (
                <div className="pagination-section">
                  <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                    showInfo={true}
                    itemsPerPage={itemsPerPage}
                    totalItems={totalItems}
                    startIndex={startIndex}
                    endIndex={endIndex}
                  />
                </div>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
};

export default LocationList;