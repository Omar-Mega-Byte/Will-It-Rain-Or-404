package com.weather_found.weather_app.modules.user.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.weather_found.weather_app.modules.shared.utils.JwtUtils;
import com.weather_found.weather_app.modules.user.dto.request.LoginRequestDto;
import com.weather_found.weather_app.modules.user.dto.request.PasswordResetConfirmDto;
import com.weather_found.weather_app.modules.user.dto.request.PasswordResetRequestDto;
import com.weather_found.weather_app.modules.user.dto.request.RefreshTokenRequestDto;
import com.weather_found.weather_app.modules.user.dto.request.UserCreateDto;
import com.weather_found.weather_app.modules.user.dto.response.JwtResponseDto;
import com.weather_found.weather_app.modules.user.dto.response.UserResponseDto;
import com.weather_found.weather_app.modules.user.exception.DatabaseOperationException;
import com.weather_found.weather_app.modules.user.exception.InvalidUserException;
import com.weather_found.weather_app.modules.user.mapper.UserMapper;
import com.weather_found.weather_app.modules.user.model.Role;
import com.weather_found.weather_app.modules.user.model.User;
import com.weather_found.weather_app.modules.user.model.UserRole;
import com.weather_found.weather_app.modules.user.repository.RoleRepository;
import com.weather_found.weather_app.modules.user.repository.UserRepository;
import com.weather_found.weather_app.modules.user.repository.UserRoleRepository;
import com.weather_found.weather_app.modules.user.validation.UserValidation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserValidation userValidation;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    public AuthService(UserValidation userValidation, UserRepository userRepository,
            RoleRepository roleRepository, UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder, UserMapper userMapper, JwtUtils jwtUtils) {
        this.userValidation = userValidation;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Registers a new user and returns JWT token
     *
     * @param userCreateDto DTO containing user registration data
     * @return DTO containing JWT token and user data
     */
    @Transactional
    public JwtResponseDto registerUser(UserCreateDto userCreateDto) {
        // Audit: Log registration attempt
        logger.info("AUDIT: User registration attempt - username: {}",
                userCreateDto != null ? userCreateDto.getUsername() : "null");

        if (userCreateDto == null) {
            logger.warn("AUDIT: Registration failed - null user data");
            throw new InvalidUserException("User registration data cannot be null");
        }

        // Validate user data
        List<String> validationErrors = userValidation.validateUserCreation(userCreateDto);
        if (userValidation.hasErrors(validationErrors)) {
            logger.warn("AUDIT: Registration failed - validation errors for username '{}': {}",
                    userCreateDto.getUsername(), validationErrors);
            throw new InvalidUserException(userValidation.formatErrors(validationErrors));
        }

        try {
            // Create normalized and encoded user data (preserving immutability)
            UserCreateDto normalizedUser = new UserCreateDto(
                    userCreateDto.getUsername().trim().toLowerCase(),
                    userCreateDto.getEmail().trim().toLowerCase(),
                    passwordEncoder.encode(userCreateDto.getPassword()),
                    userCreateDto.getFirstName().trim(),
                    userCreateDto.getLastName().trim(),
                    userCreateDto.getRole() != null ? userCreateDto.getRole().toUpperCase() : "USER");

            // Convert to entity and save
            User newUser = userMapper.toEntity(normalizedUser);
            User savedUser = userRepository.save(newUser);

            // Assign role to user
            String roleName = normalizedUser.getRole();
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new InvalidUserException("Invalid role: " + roleName));

            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRoleRepository.save(userRole);

            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(savedUser.getUsername());

            // Audit: Log successful registration
            logger.info("AUDIT: User registration successful - ID: {}, username: {}, email: {}, role: {}",
                    savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), roleName);

            UserResponseDto userResponse = userMapper.toResponseDto(savedUser);
            return JwtResponseDto.of(jwt, userResponse);

        } catch (org.springframework.dao.DataAccessException e) {
            // Audit: Log database operation failure
            logger.error("AUDIT: Database operation failed during user registration - username: {}, error: {}",
                    userCreateDto.getUsername(), e.getMessage());
            throw new DatabaseOperationException("Database operation failed during registration", e);
        } catch (Exception e) {
            // Audit: Log general registration failure
            logger.error("AUDIT: User registration failed - username: {}, error: {}",
                    userCreateDto.getUsername(), e.getMessage());
            throw new InvalidUserException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user and returns JWT token with user data.
     *
     * @param loginRequest The login request containing username and password
     * @return JWT response with token and user data
     */
    public JwtResponseDto loginUser(LoginRequestDto loginRequest) {
        // Audit: Log login attempt
        logger.info("AUDIT: User login attempt - username: {}",
                loginRequest != null && loginRequest.getUsername() != null ? loginRequest.getUsername() : "null");

        if (loginRequest == null) {
            logger.warn("AUDIT: Login failed - null login request");
            throw new InvalidUserException("Login request cannot be null");
        }

        try {
            // Step 1: Validate credentials
            userValidation.validateLoginCredentials(loginRequest.getUsername(), loginRequest.getPassword());

            // Step 2: Normalize username and fetch user
            String normalizedUsername = loginRequest.getUsername().trim().toLowerCase();
            User user = userRepository.findByUsername(normalizedUsername)
                    .orElseThrow(() -> {
                        logger.warn("AUDIT: Login failed - user not found for username '{}'", normalizedUsername);
                        return new InvalidUserException("Invalid username or password");
                    });

            // Step 3: Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                logger.warn("AUDIT: Login failed - incorrect password for username '{}'", normalizedUsername);
                throw new InvalidUserException("Invalid username or password");
            }

            // Step 4: Generate JWT token
            String jwt = jwtUtils.generateJwtToken(user.getUsername());

            // Step 5: Audit successful login
            logger.info("AUDIT: User login successful - ID: {}, username: {}", user.getId(), user.getUsername());

            UserResponseDto userResponse = userMapper.toResponseDto(user);
            return JwtResponseDto.of(jwt, userResponse);

        } catch (DataAccessException e) {
            logger.error("AUDIT: Database operation failed during user login - username: {}, error: {}",
                    loginRequest.getUsername(), e.getMessage());
            throw new DatabaseOperationException("Database operation failed during login", e);
        } catch (InvalidUserException e) {
            throw e; // Already logged
        } catch (Exception e) {
            logger.error("AUDIT: User login failed - username: {}, error: {}",
                    loginRequest.getUsername(), e.getMessage());
            throw new InvalidUserException("Login failed: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user and returns their data (legacy method for backward
     * compatibility).
     *
     * @param username The username of the user
     * @param password The password of the user
     * @return DTO containing the authenticated user data
     * @deprecated Use {@link #loginUser(LoginRequestDto)} instead
     */
    @Deprecated
    public UserResponseDto loginUser(String username, String password) {
        LoginRequestDto loginRequest = new LoginRequestDto(username, password);
        JwtResponseDto jwtResponse = loginUser(loginRequest);
        return jwtResponse.getUser();
    }

    /**
     * Logout user and invalidate token
     */
    public void logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String username = jwtUtils.getUsernameFromJwtToken(jwt);
            logger.info("AUDIT: User logout - username: {}", username);

            // TODO: Implement token blacklist for complete logout
            // For now, just log the logout event
        }
    }

    /**
     * Refresh JWT token
     */
    public JwtResponseDto refreshToken(RefreshTokenRequestDto refreshRequest) {
        logger.info("AUDIT: Token refresh attempt");

        try {
            String refreshToken = refreshRequest.getRefreshToken();

            // Validate the refresh token
            if (!jwtUtils.validateJwtToken(refreshToken)) {
                logger.warn("AUDIT: Token refresh failed - invalid refresh token");
                throw new InvalidUserException("Invalid refresh token");
            }

            String username = jwtUtils.getUsernameFromJwtToken(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("AUDIT: Token refresh failed - user not found: {}", username);
                        return new InvalidUserException("User not found");
                    });

            // Generate new JWT token
            String newJwt = jwtUtils.generateJwtToken(user.getUsername());

            logger.info("AUDIT: Token refresh successful - username: {}", username);
            UserResponseDto userResponse = userMapper.toResponseDto(user);
            return JwtResponseDto.of(newJwt, userResponse);

        } catch (Exception e) {
            logger.error("AUDIT: Token refresh failed - error: {}", e.getMessage());
            throw new InvalidUserException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(PasswordResetRequestDto resetRequest) {
        logger.info("AUDIT: Password reset request for email: {}", resetRequest.getEmail());

        try {
            String normalizedEmail = resetRequest.getEmail().trim().toLowerCase();
            User user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> {
                        logger.warn("AUDIT: Password reset failed - user not found for email: {}", normalizedEmail);
                        return new InvalidUserException("User not found with email: " + normalizedEmail);
                    });

            // TODO: Implement actual email sending with reset token
            // For now, just log the reset request
            logger.info("AUDIT: Password reset email would be sent to: {} for user: {}",
                       normalizedEmail, user.getUsername());

        } catch (DataAccessException e) {
            logger.error("AUDIT: Database error during password reset request - email: {}, error: {}",
                        resetRequest.getEmail(), e.getMessage());
            throw new DatabaseOperationException("Database operation failed during password reset", e);
        }
    }

    /**
     * Reset password using reset token
     */
    @Transactional
    public void resetPassword(PasswordResetConfirmDto resetConfirm) {
        logger.info("AUDIT: Password reset confirmation attempt");

        try {
            // TODO: Implement proper token validation and user lookup
            // For now, this is a placeholder implementation

            String token = resetConfirm.getToken();
            String newPassword = resetConfirm.getNewPassword();

            // Validate new password
            if (newPassword == null || newPassword.trim().length() < 8) {
                throw new InvalidUserException("Password must be at least 8 characters long");
            }

            // TODO: Validate reset token and get user
            // For now, just log the reset attempt
            logger.info("AUDIT: Password reset confirmation - token validation needed");

            // TODO: Update user password
            // String encodedPassword = passwordEncoder.encode(newPassword);
            // user.setPassword(encodedPassword);
            // userRepository.save(user);

            logger.info("AUDIT: Password reset would be completed for token: {}", token);

        } catch (Exception e) {
            logger.error("AUDIT: Password reset failed - error: {}", e.getMessage());
            throw new InvalidUserException("Password reset failed: " + e.getMessage());
        }
    }
}
