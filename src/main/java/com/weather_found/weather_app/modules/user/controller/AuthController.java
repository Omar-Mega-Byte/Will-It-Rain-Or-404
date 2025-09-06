package com.weather_found.weather_app.modules.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weather_found.weather_app.modules.user.dto.request.LoginRequestDto;
import com.weather_found.weather_app.modules.user.dto.request.PasswordResetConfirmDto;
import com.weather_found.weather_app.modules.user.dto.request.PasswordResetRequestDto;
import com.weather_found.weather_app.modules.user.dto.request.RefreshTokenRequestDto;
import com.weather_found.weather_app.modules.user.dto.request.UserCreateDto;
import com.weather_found.weather_app.modules.user.dto.response.JwtResponseDto;
import com.weather_found.weather_app.modules.user.dto.response.MessageResponseDto;
import com.weather_found.weather_app.modules.user.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "User authentication and account management endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register a new user - Public endpoint
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user account")
    public ResponseEntity<JwtResponseDto> registerUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        JwtResponseDto response = authService.registerUser(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user and return JWT token - Public endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<JwtResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        JwtResponseDto response = authService.loginUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user and invalidate token - Requires authentication
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "User logout", description = "Logout user and invalidate JWT token")
    public ResponseEntity<MessageResponseDto> logoutUser(HttpServletRequest request) {
        authService.logoutUser(request);
        return ResponseEntity.ok(MessageResponseDto.success("User logged out successfully"));
    }

    /**
     * Refresh JWT token - Requires authentication
     */
    @PostMapping("/refresh")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Refresh JWT token", description = "Refresh an existing JWT token")
    public ResponseEntity<JwtResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshRequest) {
        JwtResponseDto response = authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Send password reset email - Public endpoint
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email to user")
    public ResponseEntity<MessageResponseDto> forgotPassword(@Valid @RequestBody PasswordResetRequestDto resetRequest) {
        authService.sendPasswordResetEmail(resetRequest);
        return ResponseEntity.ok(MessageResponseDto.success("Password reset email sent successfully"));
    }

    /**
     * Reset password using reset token - Public endpoint
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password using reset token")
    public ResponseEntity<MessageResponseDto> resetPassword(@Valid @RequestBody PasswordResetConfirmDto resetConfirm) {
        authService.resetPassword(resetConfirm);
        return ResponseEntity.ok(MessageResponseDto.success("Password reset successfully"));
    }
}
