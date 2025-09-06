package com.weather_found.weather_app.modules.user.dto.response;

/**
 * Immutable DTO for lightweight user information in lists or references
 * Contains minimal fields for performance and privacy
 */
public class UserSummaryDto {

    private final Long id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final boolean isActive;

    public UserSummaryDto(Long id, String username, String firstName, String lastName, boolean isActive) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isActive() {
        return isActive;
    }
}
