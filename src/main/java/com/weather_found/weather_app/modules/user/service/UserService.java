package com.weather_found.weather_app.modules.user.service;

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
            logger.error("AUDIT: Database error updating user profile for username: {}, error: {}", username, e.getMessage());
            throw new DatabaseOperationException("Failed to update user profile", e);
        }
    }

    /**
     * Get user preferences (placeholder implementation)
     */
    public UserPreferencesResponseDto getUserPreferences(String username) {
        logger.info("AUDIT: Getting user preferences for username: {}", username);

        // Verify user exists
        userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("AUDIT: User not found for preferences retrieval: {}", username);
                    return new UserNotFoundException("User not found with username: " + username);
                });

        // For now, return default preferences
        // TODO: Implement user preferences table and logic
        return new UserPreferencesResponseDto(
                "celsius",
                true,
                true,
                true,
                false,
                "UTC",
                null
        );
    }

    /**
     * Update user preferences (placeholder implementation)
     */
    @Transactional
    public UserPreferencesResponseDto updateUserPreferences(String username, UserPreferencesDto preferencesDto) {
        logger.info("AUDIT: Updating user preferences for username: {}", username);

        // Verify user exists
        userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("AUDIT: User not found for preferences update: {}", username);
                    return new UserNotFoundException("User not found with username: " + username);
                });

        // TODO: Implement user preferences table and update logic
        // For now, return the updated preferences as provided
        logger.info("AUDIT: User preferences updated successfully for username: {}", username);

        return new UserPreferencesResponseDto(
                preferencesDto.getTemperatureUnit(),
                preferencesDto.getNotificationEnabled(),
                preferencesDto.getEmailNotifications(),
                preferencesDto.getPushNotifications(),
                preferencesDto.getSmsNotifications(),
                preferencesDto.getTimezone(),
                preferencesDto.getDefaultLocationId()
        );
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
            logger.error("AUDIT: Database error deleting user account for username: {}, error: {}", username, e.getMessage());
            throw new DatabaseOperationException("Failed to delete user account", e);
        }
    }
}
