import React, { useState, useEffect } from 'react';
import './LocationSearch.css';

const LocationSearch = ({ onSearch, onClear, initialQuery = '', placeholder = "Search locations..." }) => {
  const [searchQuery, setSearchQuery] = useState(initialQuery);
  const [isLoading, setIsLoading] = useState(false);
  const [searchHistory, setSearchHistory] = useState([]);

  // Load search history from localStorage on component mount
  useEffect(() => {
    const savedHistory = localStorage.getItem('locationSearchHistory');
    if (savedHistory) {
      try {
        setSearchHistory(JSON.parse(savedHistory));
      } catch (error) {
        console.error('Error loading search history:', error);
      }
    }
  }, []);

  // Save search history to localStorage
  const saveSearchHistory = (query) => {
    if (!query.trim()) return;
    
    const updatedHistory = [
      query,
      ...searchHistory.filter(item => item !== query)
    ].slice(0, 5); // Keep only last 5 searches
    
    setSearchHistory(updatedHistory);
    localStorage.setItem('locationSearchHistory', JSON.stringify(updatedHistory));
  };

  const handleSearch = async (query = searchQuery) => {
    if (!query.trim()) {
      if (onClear) {
        onClear();
      }
      return;
    }

    setIsLoading(true);
    try {
      saveSearchHistory(query);
      if (onSearch) {
        await onSearch(query.trim());
      }
    } catch (error) {
      console.error('Search error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const handleClear = () => {
    setSearchQuery('');
    if (onClear) {
      onClear();
    }
  };

  const handleHistoryClick = (query) => {
    setSearchQuery(query);
    handleSearch(query);
  };

  const clearHistory = () => {
    setSearchHistory([]);
    localStorage.removeItem('locationSearchHistory');
  };

  return (
    <div className="location-search">
      <div className="search-input-container">
        <div className="search-input-wrapper">
          <input
            type="text"
            className="search-input"
            placeholder={placeholder}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={isLoading}
          />
          
          <div className="search-buttons">
            {searchQuery && (
              <button
                className="btn btn-clear"
                onClick={handleClear}
                disabled={isLoading}
                title="Clear search"
              >
                ×
              </button>
            )}
            
            <button
              className="btn btn-search"
              onClick={() => handleSearch()}
              disabled={isLoading || !searchQuery.trim()}
              title="Search locations"
            >
              {isLoading ? (
                <span className="loading-spinner"></span>
              ) : (
                <svg className="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.35-4.35"></path>
                </svg>
              )}
            </button>
          </div>
        </div>

        {/* Search suggestions/history */}
        {searchHistory.length > 0 && searchQuery === '' && (
          <div className="search-history">
            <div className="history-header">
              <span className="history-title">Recent searches</span>
              <button className="btn btn-clear-history" onClick={clearHistory}>
                Clear history
              </button>
            </div>
            <div className="history-items">
              {searchHistory.map((query, index) => (
                <button
                  key={index}
                  className="history-item"
                  onClick={() => handleHistoryClick(query)}
                >
                  <svg className="history-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <circle cx="12" cy="12" r="3"></circle>
                    <path d="m12 1v6m0 6v6"></path>
                    <path d="m17 12h6m-6 0H1"></path>
                  </svg>
                  {query}
                </button>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Search tips */}
      <div className="search-tips">
        <small>
          💡 Try searching by city name, country, or coordinates (e.g., "New York", "Paris, France", "40.7128, -74.0060")
        </small>
      </div>
    </div>
  );
};

export default LocationSearch;