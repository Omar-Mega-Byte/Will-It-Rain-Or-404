package com.weather_found.weather_app.modules.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for JWT token refresh requests
 */
@Setter
@Getter
public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;

    public RefreshTokenRequestDto() {}

    public RefreshTokenRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
