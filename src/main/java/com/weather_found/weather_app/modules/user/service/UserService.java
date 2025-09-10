package com.weather_found.weather_app.modules.user.service;

import com.weather_found.weather_app.modules.user.model.UserPreferences;
import com.weather_found.weather_app.modules.user.repository.UserPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weather_found.weather_app.modules.user.dto.request.UserPreferencesDto;
import com.weather_found.weather_app.modules.user.dto.request.UserUpdateDto;
import com.weather_found.weather_app.modules.user.dto.response.UserPreferencesResponseDto;
import com.weather_found.weather_app.modules.user.dto.response.UserResponseDto;
import com.weather_found.weather_app.modules.user.exception.DatabaseOperationException;
import com.weather_found.weather_app.modules.user.exception.UserNotFoundException;
import com.weather_found.weather_app.modules.user.mapper.UserMapper;
import com.weather_found.weather_app.modules.user.model.User;
import com.weather_found.weather_app.modules.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Get All Users - For admin purposes (not exposed via controller yet)
     */
    public List<UserResponseDto> getAllUsers() {
        logger.info("AUDIT: Retrieving all users");

        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get user profile by username
     */
    public UserResponseDto getUserProfile(String username) {
        logger.info("AUDIT: Getting user profile for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("AUDIT: User not found for username: {}", username);
                    return new UserNotFoundException("User not found with username: " + username);
                });

        return userMapper.toResponseDto(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponseDto updateUserProfile(String username, UserUpdateDto userUpdateDto) {
        logger.info("AUDIT: Updating user profile for username: {}", username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("AUDIT: User not found for profile update: {}", username);
                        return new UserNotFoundException("User not found with username: " + username);
                    });

            // Update user fields if provided
            if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().trim().isEmpty()) {
                user.setEmail(userUpdateDto.getEmail().trim().toLowerCase());
            }
            if (userUpdateDto.getFirstName() != null && !userUpdateDto.getFirstName().trim().isEmpty()) {
                user.setFirstName(userUpdateDto.getFirstName().trim());
            }
            if (userUpdateDto.getLastName() != null && !userUpdateDto.getLastName().trim().isEmpty()) {
                user.setLastName(userUpdateDto.getLastName().trim());
            }

            User savedUser = userRepository.save(user);
            logger.info("AUDIT: User profile updated successfully for username: {}", username);

            return userMapper.toResponseDto(savedUser);

        } catch (DataAccessException e) {
            logger.error("AUDIT: Database error updating user profile for username: {}, error: {}", username,
                    e.getMessage());
            throw new DatabaseOperationException("Failed to update user profile", e);
        }
    }

    /**
     * Get user preferences (placeholder implementation)
     */
    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesResponseDto getUserPreferences(String username) {
        logger.info("AUDIT: Getting user preferences for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("AUDIT: User not found for preferences retrieval: {}", username);
                    return new UserNotFoundException("User not found with username: " + username);
                });

        UserPreferences preferences = userPreferencesRepository.findByUser(user)
                .orElseGet(() -> {
                    // Create default preferences if not found
                    UserPreferences defaultPrefs = new UserPreferences();
                    defaultPrefs.setUser(user);
                    defaultPrefs.setTemperatureUnit("celsius");
                    defaultPrefs.setNotificationEnabled(true);
                    defaultPrefs.setEmailNotifications(true);
                    defaultPrefs.setPushNotifications(true);
                    defaultPrefs.setSmsNotifications(false);
                    defaultPrefs.setTimezone("UTC");
                    defaultPrefs.setDefaultLocationId(null);
                    userPreferencesRepository.save(defaultPrefs);
                    return defaultPrefs;
                });

        return new UserPreferencesResponseDto(
                preferences.getTemperatureUnit(),
                preferences.getNotificationEnabled(),
                preferences.getEmailNotifications(),
                preferences.getPushNotifications(),
                preferences.getSmsNotifications(),
                preferences.getTimezone(),
                preferences.getDefaultLocationId());
    }

    /**
     * Update user preferences (placeholder implementation)
     */
    @Transactional
    public UserPreferencesResponseDto updateUserPreferences(String username, UserPreferencesDto preferencesDto) {
        logger.info("AUDIT: Updating user preferences for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("AUDIT: User not found for preferences update: {}", username);
                    return new UserNotFoundException("User not found with username: " + username);
                });

        UserPreferences preferences = userPreferencesRepository.findByUser(user)
                .orElseGet(() -> {
                    UserPreferences newPrefs = new UserPreferences();
                    newPrefs.setUser(user);
                    return newPrefs;
                });

        preferences.setTemperatureUnit(preferencesDto.getTemperatureUnit());
        preferences.setNotificationEnabled(preferencesDto.getNotificationEnabled());
        preferences.setEmailNotifications(preferencesDto.getEmailNotifications());
        preferences.setPushNotifications(preferencesDto.getPushNotifications());
        preferences.setSmsNotifications(preferencesDto.getSmsNotifications());
        preferences.setTimezone(preferencesDto.getTimezone());
        preferences.setDefaultLocationId(preferencesDto.getDefaultLocationId());
        userPreferencesRepository.save(preferences);

        logger.info("AUDIT: User preferences updated successfully for username: {}", username);

        return new UserPreferencesResponseDto(
                preferences.getTemperatureUnit(),
                preferences.getNotificationEnabled(),
                preferences.getEmailNotifications(),
                preferences.getPushNotifications(),
                preferences.getSmsNotifications(),
                preferences.getTimezone(),
                preferences.getDefaultLocationId());
    }

    /**
     * Delete user account
     */
    @Transactional
    public void deleteUserAccount(String username) {
        logger.info("AUDIT: Deleting user account for username: {}", username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("AUDIT: User not found for account deletion: {}", username);
                        return new UserNotFoundException("User not found with username: " + username);
                    });

            userRepository.delete(user);
            logger.info("AUDIT: User account deleted successfully for username: {}", username);

        } catch (DataAccessException e) {
            logger.error("AUDIT: Database error deleting user account for username: {}, error: {}", username,
                    e.getMessage());
            throw new DatabaseOperationException("Failed to delete user account", e);
        }
    }
}
