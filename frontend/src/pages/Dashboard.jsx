import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './Dashboard.css';

const Dashboard = () => {
  const [location, setLocation] = useState('');
  const [selectedVariables, setSelectedVariables] = useState([]);
  const [timeFrame, setTimeFrame] = useState('day');
  const [specificDate, setSpecificDate] = useState('');
  const [month, setMonth] = useState('');
  const [season, setSeason] = useState('');

  const weatherVariables = [
    'Temperature',
    'Rainfall',
    'Windspeed',
    'Dust Concentration',
    'Snowfall',
    'Cloud Cover'
  ];

  const handleVariableChange = (variable) => {
    setSelectedVariables((prev) =>
      prev.includes(variable)
        ? prev.filter((v) => v !== variable)
        : [...prev, variable]
    );
  };

  const handleDownload = (format) => {
    const mockData = {
      location,
      variables: selectedVariables,
      timeFrame,
      date: specificDate,
      month,
      season,
      metadata: {
        units: 'Metric',
        source: 'NASA Earth Observation Data'
      }
    };
    const dataStr = format === 'json'
      ? JSON.stringify(mockData, null, 2)
      : `Location,Variables,TimeFrame,Date,Month,Season,Units,Source\n${location},"${selectedVariables.join(',')}",${timeFrame},${specificDate},${month},${season},Metric,NASA Earth Observation Data`;
    const blob = new Blob([dataStr], { type: format === 'json' ? 'application/json' : 'text/csv' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `weather_data.${format}`;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="dashboard-page">
      {/* Navigation */}
      <nav className="nav">
        <div className="nav-container">
          <div className="nav-brand">
            <Link to="/" className="brand-logo">
              <span className="logo-icon">🌦️</span>
              <span className="brand-text">WeatherVision</span>
            </Link>
          </div>
          <div className="nav-menu">
            <Link to="/events" className="nav-item">Events</Link>
            <Link to="/calendar" className="nav-item">Calendar</Link>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="container mx-auto p-6 min-h-screen flex flex-col">
        <h1 className="text-3xl font-bold text-blue-700 mb-6">Weather Probability Dashboard</h1>
        
        {/* Location Input */}
        <div className="mb-6">
          <label className="block text-lg font-medium text-gray-700 mb-2">Location</label>
          <input
            type="text"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder="Enter a place name (e.g., New York)"
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Weather Variables Selection */}
        <div className="mb-6">
          <label className="block text-lg font-medium text-gray-700 mb-2">Select Weather Variables</label>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {weatherVariables.map((variable) => (
              <label key={variable} className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  checked={selectedVariables.includes(variable)}
                  onChange={() => handleVariableChange(variable)}
                  className="h-5 w-5 text-blue-600 focus:ring-blue-500"
                />
                <span>{variable}</span>
              </label>
            ))}
          </div>
        </div>

        {/* Time Selection */}
        <div className="mb-6">
          <label className="block text-lg font-medium text-gray-700 mb-2">Time Frame</label>
          <select
            value={timeFrame}
            onChange={(e) => setTimeFrame(e.target.value)}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="day">Specific Day</option>
            <option value="month">Month</option>
            <option value="season">Season</option>
          </select>

          {timeFrame === 'day' && (
            <input
              type="date"
              value={specificDate}
              onChange={(e) => setSpecificDate(e.target.value)}
              className="mt-4 w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          )}
          {timeFrame === 'month' && (
            <select
              value={month}
              onChange={(e) => setMonth(e.target.value)}
              className="mt-4 w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Select Month</option>
              {['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'].map((m) => (
                <option key={m} value={m}>{m}</option>
              ))}
            </select>
          )}
          {timeFrame === 'season' && (
            <select
              value={season}
              onChange={(e) => setSeason(e.target.value)}
              className="mt-4 w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Select Season</option>
              <option value="Spring">Spring</option>
              <option value="Summer">Summer</option>
              <option value="Fall">Fall</option>
              <option value="Winter">Winter</option>
            </select>
          )}
        </div>

        {/* Data Visualization Placeholder */}
        <div className="mb-6">
          <h2 className="text-xl font-semibold text-gray-700 mb-4">Data Visualization</h2>
          <div className="bg-gray-100 p-6 rounded-lg text-center">
            <p className="text-gray-600">Visualization Placeholder: Graphs, Maps, or Time Series will appear here.</p>
            <p className="text-gray-500 mt-2">Example: Bell curve for {selectedVariables.join(', ') || 'selected variables'} probability in {location || 'your location'}.</p>
          </div>
        </div>

        {/* Download Options */}
        <div className="flex space-x-4">
          <button
            onClick={() => handleDownload('csv')}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition"
            disabled={!location || selectedVariables.length === 0}
          >
            Download CSV
          </button>
          <button
            onClick={() => handleDownload('json')}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition"
            disabled={!location || selectedVariables.length === 0}
          >
            Download JSON
          </button>
        </div>
      </div>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-brand">
              <div className="brand-logo">
                <span className="logo-icon">🌦️</span>
                <span className="brand-text">WeatherVision</span>
              </div>
              <p className="footer-description">
                Advanced weather intelligence powered by NASA data and artificial intelligence.
              </p>
            </div>

            <div className="link-group">
              <h4 className="link-title">Product</h4>
              <Link to="/features" className="link-item">Features</Link>
              <Link to="/pricing" className="link-item">Pricing</Link>
              <Link to="/api" className="link-item">API</Link>
              <Link to="/mobile" className="link-item">Mobile Apps</Link>
            </div>

            <div className="link-group">
              <h4 className="link-title">Company</h4>
              <Link to="/about" className="link-item">About</Link>
              <Link to="/careers" className="link-item">Careers</Link>
              <Link to="/press" className="link-item">Press</Link>
              <Link to="/contact" className="link-item">Contact</Link>
            </div>

            <div className="link-group">
              <h4 className="link-title">Resources</h4>
              <Link to="/docs" className="link-item">Documentation</Link>
              <Link to="/help" className="link-item">Help Center</Link>
              <Link to="/status" className="link-item">Status</Link>
              <Link to="/blog" className="link-item">Blog</Link>
            </div>
          </div>

          <div className="footer-bottom">
            <div className="footer-bottom-content">
              <p className="copyright">© 2025 WeatherVision. All rights reserved.</p>
              <div className="legal-links">
                <Link to="/privacy" className="link-item">Privacy</Link>
                <Link to="/terms" className="link-item">Terms</Link>
                <Link to="/cookies" className="link-item">Cookies</Link>
              </div>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Dashboard;