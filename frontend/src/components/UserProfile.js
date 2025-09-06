import React, { useState, useEffect } from 'react';
import userService from '../services/userService';
import authService from '../services/authService';

const UserProfile = () => {
  const [profile, setProfile] = useState({
    email: '',
    firstName: '',
    lastName: ''
  });
  const [preferences, setPreferences] = useState({
    temperatureUnit: 'celsius',
    notificationEnabled: true,
    emailNotifications: true,
    pushNotifications: true,
    smsNotifications: false,
    timezone: 'UTC'
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [activeTab, setActiveTab] = useState('profile');

  useEffect(() => {
    loadUserData();
  }, []);

  const loadUserData = async () => {
    try {
      setLoading(true);
      const [profileData, preferencesData] = await Promise.all([
        userService.getUserProfile(),
        userService.getUserPreferences()
      ]);

      setProfile({
        email: profileData.email || '',
        firstName: profileData.firstName || '',
        lastName: profileData.lastName || ''
      });

      setPreferences(preferencesData);
    } catch (error) {
      setError('Failed to load user data: ' + error);
    } finally {
      setLoading(false);
    }
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    try {
      setError('');
      setSuccess('');

      await userService.updateUserProfile(profile);
      setSuccess('Profile updated successfully!');
    } catch (error) {
      setError('Failed to update profile: ' + error);
    }
  };

  const handlePreferencesSubmit = async (e) => {
    e.preventDefault();
    try {
      setError('');
      setSuccess('');

      await userService.updateUserPreferences(preferences);
      setSuccess('Preferences updated successfully!');
    } catch (error) {
      setError('Failed to update preferences: ' + error);
    }
  };

  const handleDeleteAccount = async () => {
    if (window.confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
      try {
        await userService.deleteUserAccount();
        window.location.href = '/';
      } catch (error) {
        setError('Failed to delete account: ' + error);
      }
    }
  };

  const handleInputChange = (e, section) => {
    const { name, value, type, checked } = e.target;
    const newValue = type === 'checkbox' ? checked : value;

    if (section === 'profile') {
      setProfile(prev => ({ ...prev, [name]: newValue }));
    } else {
      setPreferences(prev => ({ ...prev, [name]: newValue }));
    }
  };

  if (loading) {
    return <div className="text-center py-8">Loading...</div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">User Profile</h1>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {success && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {success}
        </div>
      )}

      {/* Tab Navigation */}
      <div className="border-b border-gray-200 mb-6">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('profile')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'profile'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Profile Information
          </button>
          <button
            onClick={() => setActiveTab('preferences')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'preferences'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Preferences
          </button>
          <button
            onClick={() => setActiveTab('account')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'account'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Account Settings
          </button>
        </nav>
      </div>

      {/* Profile Information Tab */}
      {activeTab === 'profile' && (
        <form onSubmit={handleProfileSubmit} className="space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">
              Email
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={profile.email}
              onChange={(e) => handleInputChange(e, 'profile')}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              required
            />
          </div>

          <div>
            <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">
              First Name
            </label>
            <input
              type="text"
              id="firstName"
              name="firstName"
              value={profile.firstName}
              onChange={(e) => handleInputChange(e, 'profile')}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">
              Last Name
            </label>
            <input
              type="text"
              id="lastName"
              name="lastName"
              value={profile.lastName}
              onChange={(e) => handleInputChange(e, 'profile')}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <button
            type="submit"
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            Update Profile
          </button>
        </form>
      )}

      {/* Preferences Tab */}
      {activeTab === 'preferences' && (
        <form onSubmit={handlePreferencesSubmit} className="space-y-4">
          <div>
            <label htmlFor="temperatureUnit" className="block text-sm font-medium text-gray-700">
              Temperature Unit
            </label>
            <select
              id="temperatureUnit"
              name="temperatureUnit"
              value={preferences.temperatureUnit}
              onChange={(e) => handleInputChange(e, 'preferences')}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="celsius">Celsius</option>
              <option value="fahrenheit">Fahrenheit</option>
            </select>
          </div>

          <div>
            <label htmlFor="timezone" className="block text-sm font-medium text-gray-700">
              Timezone
            </label>
            <input
              type="text"
              id="timezone"
              name="timezone"
              value={preferences.timezone}
              onChange={(e) => handleInputChange(e, 'preferences')}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="UTC"
            />
          </div>

          <div className="space-y-3">
            <h3 className="text-lg font-medium text-gray-700">Notification Preferences</h3>

            <div className="flex items-center">
              <input
                type="checkbox"
                id="notificationEnabled"
                name="notificationEnabled"
                checked={preferences.notificationEnabled}
                onChange={(e) => handleInputChange(e, 'preferences')}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="notificationEnabled" className="ml-2 block text-sm text-gray-700">
                Enable Notifications
              </label>
            </div>

            <div className="flex items-center">
              <input
                type="checkbox"
                id="emailNotifications"
                name="emailNotifications"
                checked={preferences.emailNotifications}
                onChange={(e) => handleInputChange(e, 'preferences')}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="emailNotifications" className="ml-2 block text-sm text-gray-700">
                Email Notifications
              </label>
            </div>

            <div className="flex items-center">
              <input
                type="checkbox"
                id="pushNotifications"
                name="pushNotifications"
                checked={preferences.pushNotifications}
                onChange={(e) => handleInputChange(e, 'preferences')}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="pushNotifications" className="ml-2 block text-sm text-gray-700">
                Push Notifications
              </label>
            </div>

            <div className="flex items-center">
              <input
                type="checkbox"
                id="smsNotifications"
                name="smsNotifications"
                checked={preferences.smsNotifications}
                onChange={(e) => handleInputChange(e, 'preferences')}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="smsNotifications" className="ml-2 block text-sm text-gray-700">
                SMS Notifications
              </label>
            </div>
          </div>

          <button
            type="submit"
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            Update Preferences
          </button>
        </form>
      )}

      {/* Account Settings Tab */}
      {activeTab === 'account' && (
        <div className="space-y-6">
          <div className="bg-red-50 border border-red-200 rounded-md p-4">
            <h3 className="text-lg font-medium text-red-800 mb-2">Danger Zone</h3>
            <p className="text-sm text-red-600 mb-4">
              Once you delete your account, there is no going back. Please be certain.
            </p>
            <button
              onClick={handleDeleteAccount}
              className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
            >
              Delete Account
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserProfile;
