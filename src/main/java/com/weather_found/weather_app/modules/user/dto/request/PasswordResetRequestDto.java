package com.weather_found.weather_app.modules.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for password reset requests
 */
@Setter
@Getter
public class PasswordResetRequestDto {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    public PasswordResetRequestDto() {}

    public PasswordResetRequestDto(String email) {
        this.email = email;
    }

}
