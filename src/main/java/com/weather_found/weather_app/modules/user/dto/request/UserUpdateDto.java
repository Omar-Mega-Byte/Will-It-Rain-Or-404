package com.weather_found.weather_app.modules.user.dto.request;

/**
 * Immutable DTO for updating existing user information
 * Contains only fields that users should be allowed to modify
 */
public class UserUpdateDto {

    private final String email;
    private final String firstName;
    private final String lastName;

    public UserUpdateDto(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Default constructor for frameworks that need it
    public UserUpdateDto() {
        this(null, null, null);
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        UserUpdateDto that = (UserUpdateDto) obj;

        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
        return lastName != null ? lastName.equals(that.lastName) : that.lastName == null;
    }

    @Override
    public int hashCode() {
        int result = email != null ? email.hashCode() : 0;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserUpdateDto{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    // Note: Username is excluded to prevent confusion and maintain consistency
    // Note: Password updates should use a separate ChangePasswordDto for security
    // Note: isActive should only be modified by admins through separate endpoints
    // Note: System fields (id, timestamps) are never user-modifiable
}
