package com.weather_found.weather_app.modules.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weather_found.weather_app.modules.user.dto.request.UserPreferencesDto;
import com.weather_found.weather_app.modules.user.dto.request.UserUpdateDto;
import com.weather_found.weather_app.modules.user.dto.response.MessageResponseDto;
import com.weather_found.weather_app.modules.user.dto.response.UserPreferencesResponseDto;
import com.weather_found.weather_app.modules.user.dto.response.UserResponseDto;
import com.weather_found.weather_app.modules.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

/**
 * REST controller for user profile and preferences management
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "User Management", description = "User profile and preferences management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Get all users - Admin only endpoint
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve all users in the system (Admin only)")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user profile", description = "Get the current authenticated user's profile")
    public ResponseEntity<UserResponseDto> getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        UserResponseDto userProfile = userService.getUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Update current user profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update user profile", description = "Update the current authenticated user's profile information")
    public ResponseEntity<UserResponseDto> updateUserProfile(
            @Valid @RequestBody UserUpdateDto userUpdateDto,
            Authentication authentication) {
        String username = authentication.getName();
        UserResponseDto updatedUser = userService.updateUserProfile(username, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Get current user preferences
     */
    @GetMapping("/preferences")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user preferences", description = "Get the current authenticated user's preferences")
    public ResponseEntity<UserPreferencesResponseDto> getUserPreferences(Authentication authentication) {
        String username = authentication.getName();
        UserPreferencesResponseDto preferences = userService.getUserPreferences(username);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Update current user preferences
     */
    @PutMapping("/preferences")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update user preferences", description = "Update the current authenticated user's preferences")
    public ResponseEntity<UserPreferencesResponseDto> updateUserPreferences(
            @Valid @RequestBody UserPreferencesDto preferencesDto,
            Authentication authentication) {
        String username = authentication.getName();
        UserPreferencesResponseDto updatedPreferences = userService.updateUserPreferences(username, preferencesDto);
        return ResponseEntity.ok(updatedPreferences);
    }

    /**
     * Delete current user account
     */
    @DeleteMapping("/account")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Delete user account", description = "Delete the current authenticated user's account")
    public ResponseEntity<MessageResponseDto> deleteUserAccount(Authentication authentication) {
        String username = authentication.getName();
        userService.deleteUserAccount(username);
        return ResponseEntity.ok(MessageResponseDto.success("User account deleted successfully"));
    }
}
