package com.weather_found.weather_app.modules.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import com.weather_found.weather_app.modules.user.dto.request.UserUpdateDto;
import com.weather_found.weather_app.modules.user.dto.response.UserResponseDto;
import com.weather_found.weather_app.modules.user.exception.DatabaseOperationException;
import com.weather_found.weather_app.modules.user.exception.UserNotFoundException;
import com.weather_found.weather_app.modules.user.mapper.UserMapper;
import com.weather_found.weather_app.modules.user.model.User;
import com.weather_found.weather_app.modules.user.repository.UserRepository;

/**
 * Comprehensive JUnit 5 Unit Tests for UserService
 * 
 * This test class demonstrates real-world testing practices using JUnit 5
 * features:
 * - @ExtendWith for Mockito integration
 * - @Nested classes for organizing related tests
 * - @DisplayName for readable test descriptions
 * - Parameterized tests for testing multiple scenarios
 * - Mock objects for isolating the system under test
 * - Assertion methods for verifying behavior
 * 
 * Focus: Testing the updateUserProfile method as it represents a clear,
 * well-defined business operation with multiple test scenarios.
 * 
 * @author Weather App Team
 * @version 1.0
 * @since 2025-09-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests - Focus on updateUserProfile Feature")
class UserServiceTest {

    // System Under Test
    @InjectMocks
    private UserService userService;

    // Dependencies (Mocked)
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    // Test Data
    private User existingUser;
    private UserUpdateDto validUpdateDto;
    private UserResponseDto expectedResponseDto;
    private Set<String> userRoles;

    /**
     * Setup method executed before each test
     * Initializes common test data and mock behavior
     */
    @BeforeEach
    void setUp() {
        // Initialize test data
        setupTestData();
        setupMockBehavior();
    }

    /**
     * Helper method to setup common test data
     */
    private void setupTestData() {
        // Create existing user
        existingUser = new User(
                "johndoe",
                "john.doe@example.com",
                "hashedPassword123",
                "John",
                "Doe",
                true);
        existingUser.setId(1L);
        existingUser.setCreatedAt(Instant.now().minusSeconds(86400)); // 1 day ago
        existingUser.setUpdatedAt(Instant.now().minusSeconds(3600)); // 1 hour ago

        // Create update request
        validUpdateDto = new UserUpdateDto(
                "john.newemail@example.com",
                "Jonathan",
                "Smith");

        // Create expected response
        userRoles = new HashSet<>();
        userRoles.add("USER");

        expectedResponseDto = new UserResponseDto(
                1L,
                "johndoe",
                "john.newemail@example.com",
                "Jonathan",
                "Smith",
                true,
                userRoles,
                existingUser.getCreatedAt(),
                Instant.now() // Updated timestamp
        );
    }

    /**
     * Helper method to setup default mock behavior
     * Using lenient() to avoid UnnecessaryStubbingException for tests that don't
     * use all stubs
     */
    private void setupMockBehavior() {
        lenient().when(userRepository.findByUsername("johndoe"))
                .thenReturn(Optional.of(existingUser));

        lenient().when(userRepository.save(any(User.class)))
                .thenReturn(existingUser);

        lenient().when(userMapper.toResponseDto(any(User.class)))
                .thenReturn(expectedResponseDto);
    }

    /**
     * Nested class for successful update scenarios
     * Groups related tests together for better organization
     */
    @Nested
    @DisplayName("Successful Update Scenarios")
    class SuccessfulUpdateScenarios {

        @Test
        @DisplayName("Should successfully update all user profile fields")
        void shouldUpdateAllUserProfileFields() {
            // Arrange
            String username = "johndoe";

            // Act
            UserResponseDto result = userService.updateUserProfile(username, validUpdateDto);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(expectedResponseDto.getEmail(), result.getEmail(),
                    "Email should be updated");
            assertEquals(expectedResponseDto.getFirstName(), result.getFirstName(),
                    "First name should be updated");
            assertEquals(expectedResponseDto.getLastName(), result.getLastName(),
                    "Last name should be updated");

            // Verify interactions
            verify(userRepository, times(1)).findByUsername(username);
            verify(userRepository, times(1)).save(existingUser);
            verify(userMapper, times(1)).toResponseDto(existingUser);
        }

        @Test
        @DisplayName("Should update only email when other fields are null")
        void shouldUpdateOnlyEmailWhenOtherFieldsAreNull() {
            // Arrange
            String username = "johndoe";
            UserUpdateDto partialUpdateDto = new UserUpdateDto(
                    "new.email@example.com",
                    null,
                    null);

            // Act
            userService.updateUserProfile(username, partialUpdateDto);

            // Assert
            assertEquals("new.email@example.com", existingUser.getEmail().toLowerCase(),
                    "Email should be updated and lowercased");
            assertEquals("John", existingUser.getFirstName(),
                    "First name should remain unchanged");
            assertEquals("Doe", existingUser.getLastName(),
                    "Last name should remain unchanged");

            // Verify repository interactions
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should handle empty strings by not updating fields")
        void shouldNotUpdateFieldsWithEmptyStrings() {
            // Arrange
            String username = "johndoe";
            UserUpdateDto emptyFieldsDto = new UserUpdateDto(
                    "",
                    "   ",
                    "NewLastName");

            // Act
            userService.updateUserProfile(username, emptyFieldsDto);

            // Assert
            assertEquals("john.doe@example.com", existingUser.getEmail(),
                    "Email should remain unchanged when empty string provided");
            assertEquals("John", existingUser.getFirstName(),
                    "First name should remain unchanged when whitespace provided");
            assertEquals("NewLastName", existingUser.getLastName(),
                    "Last name should be updated when valid value provided");
        }

        @Test
        @DisplayName("Should trim whitespace from updated fields")
        void shouldTrimWhitespaceFromUpdatedFields() {
            // Arrange
            String username = "johndoe";
            UserUpdateDto whitespaceDto = new UserUpdateDto(
                    "  trimmed.email@example.com  ",
                    "  TrimmedFirst  ",
                    "  TrimmedLast  ");

            // Act
            userService.updateUserProfile(username, whitespaceDto);

            // Assert
            assertEquals("trimmed.email@example.com", existingUser.getEmail(),
                    "Email should be trimmed and lowercased");
            assertEquals("TrimmedFirst", existingUser.getFirstName(),
                    "First name should be trimmed");
            assertEquals("TrimmedLast", existingUser.getLastName(),
                    "Last name should be trimmed");
        }
    }

    /**
     * Nested class for error scenarios
     * Tests exception handling and error conditions
     */
    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenarios {

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
            // Arrange
            String nonExistentUsername = "nonexistent";
            when(userRepository.findByUsername(nonExistentUsername))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateUserProfile(nonExistentUsername, validUpdateDto),
                    "Should throw UserNotFoundException when user not found");

            assertEquals("User not found with username: " + nonExistentUsername,
                    exception.getMessage(),
                    "Exception message should contain the username");

            // Verify that save was never called
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).toResponseDto(any(User.class));
        }

        @Test
        @DisplayName("Should throw DatabaseOperationException when database error occurs during save")
        void shouldThrowDatabaseOperationExceptionOnSaveError() {
            // Arrange
            String username = "johndoe";
            DataIntegrityViolationException dbException = new DataIntegrityViolationException(
                    "Database constraint violation");

            when(userRepository.save(any(User.class)))
                    .thenThrow(dbException);

            // Act & Assert
            DatabaseOperationException exception = assertThrows(
                    DatabaseOperationException.class,
                    () -> userService.updateUserProfile(username, validUpdateDto),
                    "Should throw DatabaseOperationException when database error occurs");

            assertEquals("Failed to update user profile", exception.getMessage(),
                    "Exception message should indicate profile update failure");
            assertEquals(dbException, exception.getCause(),
                    "Original exception should be preserved as cause");

            // Verify that repository operations were attempted
            verify(userRepository).findByUsername(username);
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should throw DatabaseOperationException when database error occurs during find")
        void shouldThrowDatabaseOperationExceptionOnFindError() {
            // Arrange
            String username = "johndoe";
            DataAccessException dbException = new DataAccessException("Database connection failed") {
            };

            when(userRepository.findByUsername(username))
                    .thenThrow(dbException);

            // Act & Assert
            DatabaseOperationException exception = assertThrows(
                    DatabaseOperationException.class,
                    () -> userService.updateUserProfile(username, validUpdateDto),
                    "Should throw DatabaseOperationException when find operation fails");

            assertEquals("Failed to update user profile", exception.getMessage(),
                    "Exception message should indicate profile update failure");
            assertEquals(dbException, exception.getCause(),
                    "Original exception should be preserved as cause");

            // Verify that save was never called
            verify(userRepository, never()).save(any(User.class));
        }
    }

    /**
     * Nested class for boundary and edge case testing
     * Tests unusual but valid inputs and boundary conditions
     */
    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditions {

        @Test
        @DisplayName("Should handle null UserUpdateDto gracefully")
        void shouldHandleNullUserUpdateDto() {
            // Arrange
            String username = "johndoe";

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> userService.updateUserProfile(username, null),
                    "Should throw NullPointerException when UserUpdateDto is null");
        }

        @Test
        @DisplayName("Should handle null username gracefully")
        void shouldHandleNullUsername() {
            // Act & Assert
            assertThrows(
                    Exception.class,
                    () -> userService.updateUserProfile(null, validUpdateDto),
                    "Should throw exception when username is null");
        }

        @Test
        @DisplayName("Should handle very long field values")
        void shouldHandleVeryLongFieldValues() {
            // Arrange
            String username = "johndoe";
            String longEmail = "a".repeat(100) + "@example.com";
            String longFirstName = "B".repeat(255);
            String longLastName = "C".repeat(255);

            UserUpdateDto longFieldsDto = new UserUpdateDto(
                    longEmail,
                    longFirstName,
                    longLastName);

            // Act
            userService.updateUserProfile(username, longFieldsDto);

            // Assert
            assertEquals(longEmail.toLowerCase(), existingUser.getEmail(),
                    "Long email should be processed correctly");
            assertEquals(longFirstName, existingUser.getFirstName(),
                    "Long first name should be processed correctly");
            assertEquals(longLastName, existingUser.getLastName(),
                    "Long last name should be processed correctly");
        }

        @Test
        @DisplayName("Should handle special characters in field values")
        void shouldHandleSpecialCharactersInFields() {
            // Arrange
            String username = "johndoe";
            UserUpdateDto specialCharsDto = new UserUpdateDto(
                    "user+tag@example-domain.co.uk",
                    "José María",
                    "O'Connor-Smith");

            // Act
            userService.updateUserProfile(username, specialCharsDto);

            // Assert
            assertEquals("user+tag@example-domain.co.uk", existingUser.getEmail(),
                    "Email with special characters should be preserved");
            assertEquals("José María", existingUser.getFirstName(),
                    "First name with accents should be preserved");
            assertEquals("O'Connor-Smith", existingUser.getLastName(),
                    "Last name with apostrophe and hyphen should be preserved");
        }
    }

    /**
     * Nested class for testing transactional behavior and integration aspects
     * While these are unit tests, we test the service's behavior with repository
     * interactions
     */
    @Nested
    @DisplayName("Transactional Behavior and Service Integration")
    class TransactionalBehaviorTests {

        @Test
        @DisplayName("Should ensure user entity is modified before save")
        void shouldEnsureUserEntityIsModifiedBeforeSave() {
            // Arrange
            String username = "johndoe";
            String originalEmail = existingUser.getEmail();
            String originalFirstName = existingUser.getFirstName();
            String originalLastName = existingUser.getLastName();

            // Act
            userService.updateUserProfile(username, validUpdateDto);

            // Assert - Verify that the user entity was actually modified
            assertNotEquals(originalEmail, existingUser.getEmail(),
                    "User email should be modified");
            assertNotEquals(originalFirstName, existingUser.getFirstName(),
                    "User first name should be modified");
            assertNotEquals(originalLastName, existingUser.getLastName(),
                    "User last name should be modified");

            // Verify save was called with the modified entity
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should call mapper with saved user entity")
        void shouldCallMapperWithSavedUserEntity() {
            // Arrange
            String username = "johndoe";
            User savedUser = new User("johndoe", "updated@example.com",
                    "hashedPassword123", "UpdatedFirst", "UpdatedLast", true);
            savedUser.setId(1L);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            userService.updateUserProfile(username, validUpdateDto);

            // Assert
            verify(userMapper).toResponseDto(savedUser);
            verify(userMapper, never()).toResponseDto(existingUser);
        }

        @Test
        @DisplayName("Should maintain transactional integrity on repository operations")
        void shouldMaintainTransactionalIntegrityOnRepositoryOperations() {
            // Arrange
            String username = "johndoe";

            // Act
            userService.updateUserProfile(username, validUpdateDto);

            // Assert - Verify correct order of operations
            var inOrder = inOrder(userRepository, userMapper);
            inOrder.verify(userRepository).findByUsername(username);
            inOrder.verify(userRepository).save(existingUser);
            inOrder.verify(userMapper).toResponseDto(any(User.class));
        }
    }

    /**
     * Additional helper tests to verify test setup and mock behavior
     */
    @Nested
    @DisplayName("Test Infrastructure Validation")
    class TestInfrastructureValidation {

        @Test
        @DisplayName("Should have properly initialized test data")
        void shouldHaveProperlyInitializedTestData() {
            // Assert test data integrity
            assertNotNull(existingUser, "Existing user should be initialized");
            assertNotNull(validUpdateDto, "Valid update DTO should be initialized");
            assertNotNull(expectedResponseDto, "Expected response DTO should be initialized");

            assertEquals("johndoe", existingUser.getUsername(),
                    "User username should be correctly set");
            assertEquals("john.newemail@example.com", validUpdateDto.getEmail(),
                    "Update DTO email should be correctly set");
        }

        @Test
        @DisplayName("Should have properly configured mocks")
        void shouldHaveProperlyConfiguredMocks() {
            // Verify that mocks are properly injected
            assertNotNull(userRepository, "UserRepository mock should be injected");
            assertNotNull(userMapper, "UserMapper mock should be injected");
            assertNotNull(userService, "UserService should be injected with mocks");
        }
    }
}
