package com.weather_found.weather_app.modules.user.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.weather_found.weather_app.modules.user.dto.request.UserCreateDto;
import com.weather_found.weather_app.modules.user.dto.request.UserUpdateDto;
import com.weather_found.weather_app.modules.user.dto.response.UserResponseDto;
import com.weather_found.weather_app.modules.user.dto.response.UserSummaryDto;
import com.weather_found.weather_app.modules.user.model.User;
import com.weather_found.weather_app.modules.user.service.UserRoleService;

/**
 * Simple mapper for User entity and DTOs
 * Provides conversion methods between User entity and various DTOs
 */
@Component
public class UserMapper {

    @Autowired
    private UserRoleService userRoleService;

    /**
     * Convert UserCreateDto to User entity
     * Note: Sets isActive to true by default for new users
     */
    public User toEntity(UserCreateDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // Password should be hashed in service layer
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setActive(true); // Default active status for new users
        return user;
    }

    /**
     * Convert User entity to UserResponseDto
     * Includes all non-sensitive user information and roles
     */
    public UserResponseDto toResponseDto(User user) {
        Set<String> roles = userRoleService.getUserRoles(user.getId());

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    /**
     * Convert User entity to UserSummaryDto
     * Returns minimal user information for lists and references
     */
    public UserSummaryDto toSummaryDto(User user) {
        return new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive());
    }

    /**
     * Update existing User entity with UserUpdateDto data
     * Only updates fields that are present in the DTO
     */
    public void updateEntity(User user, UserUpdateDto dto) {
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        // Note: updatedAt will be automatically set by BaseEntity
    }

    /**
     * Convert list of User entities to list of UserResponseDto
     */
    public List<UserResponseDto> toResponseDtoList(List<User> users) {
        return users.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of User entities to list of UserSummaryDto
     */
    public List<UserSummaryDto> toSummaryDtoList(List<User> users) {
        return users.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }
}
