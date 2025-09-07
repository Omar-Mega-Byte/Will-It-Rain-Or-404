import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import UpcomingEvents from '../components/UpcomingEvents';
import EventStats from '../components/EventStats';
import './EnhancedDashboard.css';

const EnhancedDashboard = () => {
  const [user, setUser] = useState(null);
  const [weather, setWeather] = useState({
    temperature: 22,
    humidity: 68,
    windSpeed: 15,
    condition: 'Partly Cloudy'
  });
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    // Get user info from token or make API call
    const userData = {
      username: 'User',
      email: 'user@example.com'
    };
    setUser(userData);
  }, [navigate]);

  const handleLogout = () => {
    authService.logout();
    navigate('/');
  };

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex items-center">
              <span className="text-2xl mr-3">🌦️</span>
              <h1 className="text-xl font-bold text-gray-900">WeatherVision Dashboard</h1>
            </div>
            
            {/* Navigation */}
            <nav className="hidden md:flex items-center space-x-8">
              <Link
                to="/dashboard"
                className="text-blue-600 hover:text-blue-700 px-3 py-2 rounded-md text-sm font-medium"
              >
                Dashboard
              </Link>
              <Link
                to="/events"
                className="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              >
                Events
              </Link>
              <Link
                to="/calendar"
                className="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              >
                Calendar
              </Link>
            </nav>

            {/* User menu */}
            <div className="flex items-center space-x-4">
              <span className="text-gray-700 text-sm">Welcome, {user.username}!</span>
              <button
                onClick={handleLogout}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            
            {/* Left Column - Weather & Stats */}
            <div className="lg:col-span-2 space-y-6">
              
              {/* Weather Card */}
              <div className="bg-white overflow-hidden shadow rounded-lg">
                <div className="p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <span className="text-4xl">🌤️</span>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="text-sm font-medium text-gray-500 truncate">Current Weather</dt>
                        <dd className="flex items-baseline">
                          <div className="text-2xl font-semibold text-gray-900">{weather.temperature}°C</div>
                          <div className="ml-2 text-sm text-gray-600">{weather.condition}</div>
                        </dd>
                      </dl>
                    </div>
                  </div>
                  
                  <div className="mt-4 grid grid-cols-2 gap-4">
                    <div className="flex items-center">
                      <span className="text-sm text-gray-500">💨 Wind:</span>
                      <span className="ml-2 text-sm font-medium text-gray-900">{weather.windSpeed} km/h</span>
                    </div>
                    <div className="flex items-center">
                      <span className="text-sm text-gray-500">💧 Humidity:</span>
                      <span className="ml-2 text-sm font-medium text-gray-900">{weather.humidity}%</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Event Statistics */}
              <EventStats showDetailedStats={false} />

            </div>

            {/* Right Column - Upcoming Events & Quick Actions */}
            <div className="space-y-6">
              
              {/* Upcoming Events */}
              <UpcomingEvents maxEvents={5} showViewAll={true} compact={true} />

              {/* Quick Actions */}
              <div className="bg-white overflow-hidden shadow rounded-lg">
                <div className="p-6">
                  <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Quick Actions</h3>
                  <div className="space-y-3">
                    <Link
                      to="/events"
                      className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                      <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                      </svg>
                      Create Event
                    </Link>
                    <Link
                      to="/calendar"
                      className="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                      <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      View Calendar
                    </Link>
                  </div>
                </div>
              </div>

              {/* Weather Stats */}
              <div className="bg-white overflow-hidden shadow rounded-lg">
                <div className="p-6">
                  <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Weather Stats</h3>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="text-center">
                      <div className="text-2xl font-bold text-blue-600">99.2%</div>
                      <div className="text-xs text-gray-500">Accuracy</div>
                    </div>
                    <div className="text-center">
                      <div className="text-2xl font-bold text-green-600">0.3s</div>
                      <div className="text-xs text-gray-500">Response</div>
                    </div>
                    <div className="text-center">
                      <div className="text-2xl font-bold text-purple-600">12</div>
                      <div className="text-xs text-gray-500">Satellites</div>
                    </div>
                    <div className="text-center">
                      <div className="text-2xl font-bold text-indigo-600">Global</div>
                      <div className="text-xs text-gray-500">Coverage</div>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default EnhancedDashboard;
