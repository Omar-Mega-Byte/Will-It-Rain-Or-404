package com.weather_found.weather_app.modules.user.dto.response;

/**
 * Immutable DTO for JWT authentication responses
 */
public class JwtResponseDto {
    private final String token;
    private final String type;
    private final UserResponseDto user;

    public JwtResponseDto(String token, String type, UserResponseDto user) {
        this.token = token;
        this.type = type;
        this.user = user;
    }

    public static JwtResponseDto of(String token, UserResponseDto user) {
        return new JwtResponseDto(token, "Bearer", user);
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public UserResponseDto getUser() {
        return user;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        JwtResponseDto that = (JwtResponseDto) obj;

        if (token != null ? !token.equals(that.token) : that.token != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null)
            return false;
        return user != null ? user.equals(that.user) : that.user == null;
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JwtResponseDto{" +
                "token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", user=" + user +
                '}';
    }
}
