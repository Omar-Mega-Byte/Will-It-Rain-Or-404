# JUnit 5 User Service Tests Documentation

## Overview

This document provides comprehensive documentation for the JUnit 5 unit tests implemented for the **User Profile Update** feature in the `UserService` class. These tests demonstrate real-world testing practices and showcase various JUnit 5 features.

## 🎯 Why User Profile Update Feature?

The `updateUserProfile` method was chosen for testing because it represents:

- **Clear Business Logic**: Updates user profile information with validation
- **Multiple Test Scenarios**: Success, failure, edge cases, and boundary conditions
- **Real-World Complexity**: Error handling, data validation, and transactional behavior
- **Good Testing Candidate**: Perfect for demonstrating various testing techniques

## 🧪 Test Structure Overview

### Test Class Organization

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests - Focus on updateUserProfile Feature")
class UserServiceTest
```

The test class uses:
- `@ExtendWith(MockitoExtension.class)` - Integrates Mockito with JUnit 5
- `@DisplayName` - Provides readable test descriptions
- Nested classes for logical grouping of related tests

### Dependencies and Mocking

```java
@InjectMocks
private UserService userService;  // System Under Test

@Mock
private UserRepository userRepository;  // Mocked dependency

@Mock
private UserMapper userMapper;  // Mocked dependency
```

## 📋 Test Categories

### 1. Successful Update Scenarios (`@Nested` class)

**Purpose**: Test happy path scenarios where operations complete successfully.

#### Test Cases:

1. **`shouldUpdateAllUserProfileFields()`**
   - **What it tests**: Complete profile update with all fields
   - **Scenario**: User provides email, firstName, and lastName
   - **Verification**: All fields are updated correctly
   - **Mockito Usage**: Verifies repository and mapper interactions

2. **`shouldUpdateOnlyEmailWhenOtherFieldsAreNull()`**
   - **What it tests**: Partial updates with null values
   - **Scenario**: Only email is provided, other fields are null
   - **Verification**: Only email is updated, other fields remain unchanged

3. **`shouldNotUpdateFieldsWithEmptyStrings()`**
   - **What it tests**: Empty string handling
   - **Scenario**: Empty strings and whitespace-only strings provided
   - **Verification**: Empty fields are ignored, valid fields are updated

4. **`shouldTrimWhitespaceFromUpdatedFields()`**
   - **What it tests**: Data sanitization
   - **Scenario**: Input values contain leading/trailing whitespace
   - **Verification**: Whitespace is trimmed, email is lowercased

### 2. Error Scenarios (`@Nested` class)

**Purpose**: Test exception handling and error conditions.

#### Test Cases:

1. **`shouldThrowUserNotFoundExceptionWhenUserDoesNotExist()`**
   - **What it tests**: User not found error handling
   - **Scenario**: Username doesn't exist in database
   - **Expected**: `UserNotFoundException` thrown with correct message
   - **Verification**: Repository save is never called

2. **`shouldThrowDatabaseOperationExceptionOnSaveError()`**
   - **What it tests**: Database error during save operation
   - **Scenario**: Repository throws `DataIntegrityViolationException`
   - **Expected**: `DatabaseOperationException` wrapping original exception
   - **Verification**: Original exception is preserved as cause

3. **`shouldThrowDatabaseOperationExceptionOnFindError()`**
   - **What it tests**: Database error during find operation
   - **Scenario**: Repository throws `DataAccessException` on find
   - **Expected**: Exception propagated to caller
   - **Verification**: Save operation is never attempted

### 3. Edge Cases and Boundary Conditions (`@Nested` class)

**Purpose**: Test unusual but valid inputs and boundary conditions.

#### Test Cases:

1. **`shouldHandleNullUserUpdateDto()`**
   - **What it tests**: Null input handling
   - **Expected**: `NullPointerException` thrown

2. **`shouldHandleNullUsername()`**
   - **What it tests**: Null username handling
   - **Expected**: Exception thrown

3. **`shouldHandleVeryLongFieldValues()`**
   - **What it tests**: Large input values
   - **Scenario**: Very long strings for each field
   - **Verification**: Values are processed correctly

4. **`shouldHandleSpecialCharactersInFields()`**
   - **What it tests**: Special character handling
   - **Scenario**: Unicode characters, symbols, hyphens in names
   - **Verification**: Special characters are preserved correctly

### 4. Transactional Behavior Tests (`@Nested` class)

**Purpose**: Test service behavior and integration aspects.

#### Test Cases:

1. **`shouldEnsureUserEntityIsModifiedBeforeSave()`**
   - **What it tests**: Entity modification verification
   - **Verification**: User entity is actually modified before save

2. **`shouldCallMapperWithSavedUserEntity()`**
   - **What it tests**: Mapper called with correct entity
   - **Verification**: Mapper receives the saved entity, not original

3. **`shouldMaintainTransactionalIntegrityOnRepositoryOperations()`**
   - **What it tests**: Operation order verification
   - **Verification**: Operations occur in correct sequence using `inOrder()`

### 5. Test Infrastructure Validation (`@Nested` class)

**Purpose**: Verify test setup and mock configuration.

#### Test Cases:

1. **`shouldHaveProperlyInitializedTestData()`**
   - **What it tests**: Test data integrity
   - **Verification**: All test objects are properly initialized

2. **`shouldHaveProperlyConfiguredMocks()`**
   - **What it tests**: Mock injection
   - **Verification**: All mocks are properly injected

## 🚀 JUnit 5 Features Demonstrated

### 1. **@ExtendWith(MockitoExtension.class)**
- Integrates Mockito with JUnit 5
- Automatically initializes mocks and injects them
- Replaces the old `@RunWith(MockitoJUnitRunner.class)` from JUnit 4

### 2. **@DisplayName**
- Provides human-readable test names
- Improves test reports and IDE display
- Makes tests self-documenting

### 3. **@Nested Classes**
- Organizes related tests into logical groups
- Provides hierarchical test structure
- Allows sharing setup between related tests

### 4. **@BeforeEach**
- Runs before each test method
- Sets up common test data and mock behavior
- Ensures clean state for each test

### 5. **Assertion Methods**
- `assertNotNull()` - Verifies object is not null
- `assertEquals()` - Verifies expected vs actual values
- `assertThrows()` - Verifies exceptions are thrown
- `assertNotEquals()` - Verifies values are different

### 6. **@Mock and @InjectMocks**
- `@Mock` creates mock objects
- `@InjectMocks` creates instance with mocked dependencies injected

## 🔧 Mockito Features Demonstrated

### 1. **Mock Configuration**
```java
when(userRepository.findByUsername("johndoe"))
    .thenReturn(Optional.of(existingUser));
```

### 2. **Argument Matchers**
```java
when(userRepository.save(any(User.class)))
    .thenReturn(existingUser);
```

### 3. **Verification**
```java
verify(userRepository, times(1)).findByUsername(username);
verify(userRepository, never()).save(any(User.class));
```

### 4. **Exception Throwing**
```java
when(userRepository.save(any(User.class)))
    .thenThrow(new DataIntegrityViolationException("Database error"));
```

### 5. **Ordered Verification**
```java
var inOrder = inOrder(userRepository, userMapper);
inOrder.verify(userRepository).findByUsername(username);
inOrder.verify(userRepository).save(existingUser);
```

## 📝 Testing Best Practices Demonstrated

### 1. **AAA Pattern (Arrange-Act-Assert)**
Each test follows the clear structure:
```java
// Arrange - Set up test data and mocks
String username = "johndoe";

// Act - Execute the method under test
UserResponseDto result = userService.updateUserProfile(username, validUpdateDto);

// Assert - Verify the results
assertNotNull(result);
verify(userRepository).save(existingUser);
```

### 2. **Test Isolation**
- Each test is independent
- Mocks are reset between tests
- No shared state between tests

### 3. **Descriptive Test Names**
- Tests clearly describe what they're testing
- Use `@DisplayName` for human-readable descriptions

### 4. **Comprehensive Coverage**
- Happy path scenarios
- Error conditions
- Edge cases and boundary conditions
- Integration behavior

### 5. **Mock Verification**
- Verify method calls and parameters
- Verify call counts (`times(1)`, `never()`)
- Verify call order when important

## 🏃‍♂️ How to Run the Tests

### 1. **Using Maven**
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run specific test method
mvn test -Dtest=UserServiceTest#shouldUpdateAllUserProfileFields
```

### 2. **Using IDE (VS Code, IntelliJ, Eclipse)**
- Right-click on test class → "Run Tests"
- Right-click on test method → "Run Test"
- Use keyboard shortcuts (usually Ctrl+Shift+F10)

### 3. **Using Gradle (if applicable)**
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests UserServiceTest
```

## 📊 Test Reports

When you run the tests, you'll get detailed reports showing:

- **Test Results**: Pass/Fail status for each test
- **Test Names**: Readable descriptions from `@DisplayName`
- **Hierarchical Structure**: Nested test organization
- **Error Details**: Stack traces for failing tests
- **Coverage Information**: Code coverage metrics

### Sample Test Output
```
UserService Tests - Focus on updateUserProfile Feature
├── Successful Update Scenarios
│   ├── ✓ Should successfully update all user profile fields
│   ├── ✓ Should update only email when other fields are null
│   ├── ✓ Should handle empty strings by not updating fields
│   └── ✓ Should trim whitespace from updated fields
├── Error Scenarios
│   ├── ✓ Should throw UserNotFoundException when user does not exist
│   ├── ✓ Should throw DatabaseOperationException when database error occurs during save
│   └── ✓ Should throw DatabaseOperationException when database error occurs during find
├── Edge Cases and Boundary Conditions
│   ├── ✓ Should handle null UserUpdateDto gracefully
│   ├── ✓ Should handle null username gracefully
│   ├── ✓ Should handle very long field values
│   └── ✓ Should handle special characters in field values
├── Transactional Behavior and Service Integration
│   ├── ✓ Should ensure user entity is modified before save
│   ├── ✓ Should call mapper with saved user entity
│   └── ✓ Should maintain transactional integrity on repository operations
└── Test Infrastructure Validation
    ├── ✓ Should have properly initialized test data
    └── ✓ Should have properly configured mocks

Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
```

## 🎓 Learning Outcomes

After studying these tests, you should understand:

1. **JUnit 5 Architecture**: How to use modern JUnit 5 features
2. **Mock Testing**: How to isolate units under test using Mockito
3. **Test Organization**: How to structure tests for maintainability
4. **Error Testing**: How to test exception scenarios effectively
5. **Boundary Testing**: How to test edge cases and limits
6. **Integration Testing**: How to test service interactions
7. **Best Practices**: Industry-standard testing approaches

## 🔄 Next Steps

To extend these tests, consider adding:

1. **Parameterized Tests**: Test multiple inputs with `@ParameterizedTest`
2. **Dynamic Tests**: Generate tests at runtime with `@TestFactory`
3. **Performance Tests**: Measure execution time with `@Timeout`
4. **Integration Tests**: Test with real database using `@SpringBootTest`
5. **Test Slices**: Use `@DataJpaTest` for repository testing

## 📚 Additional Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [Test-Driven Development Best Practices](https://martinfowler.com/bliki/TestDrivenDevelopment.html)

---

*This documentation serves as both a learning resource and a reference for maintaining and extending the test suite.*
