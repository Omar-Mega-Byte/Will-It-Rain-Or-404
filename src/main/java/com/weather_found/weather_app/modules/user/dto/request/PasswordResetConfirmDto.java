package com.weather_found.weather_app.modules.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for password reset confirmation
 */
@Setter
@Getter
public class PasswordResetConfirmDto {

    @NotBlank(message = "Reset token cannot be blank")
    private String token;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String newPassword;

    public PasswordResetConfirmDto() {}

    public PasswordResetConfirmDto(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

}
