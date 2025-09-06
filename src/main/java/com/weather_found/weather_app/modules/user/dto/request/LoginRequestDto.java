package com.weather_found.weather_app.modules.user.dto.request;

/**
 * Immutable DTO for user login requests
 */
public class LoginRequestDto {
    private final String username;
    private final String password;

    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
