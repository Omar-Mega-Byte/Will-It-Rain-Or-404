package com.weather_found.weather_app.modules.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating user preferences
 */
@Setter
@Getter
public class UserPreferencesDto {

    // Getters and setters
    @NotNull(message = "Temperature unit cannot be null")
    private String temperatureUnit = "celsius"; // celsius, fahrenheit

    @NotNull(message = "Notification enabled flag cannot be null")
    private Boolean notificationEnabled = true;

    @NotNull(message = "Email notifications flag cannot be null")
    private Boolean emailNotifications = true;

    @NotNull(message = "Push notifications flag cannot be null")
    private Boolean pushNotifications = true;

    @NotNull(message = "SMS notifications flag cannot be null")
    private Boolean smsNotifications = false;

    private String timezone = "UTC";

    private Long defaultLocationId;

    public UserPreferencesDto() {}

    public UserPreferencesDto(String temperatureUnit, Boolean notificationEnabled,
                             Boolean emailNotifications, Boolean pushNotifications,
                             Boolean smsNotifications, String timezone, Long defaultLocationId) {
        this.temperatureUnit = temperatureUnit;
        this.notificationEnabled = notificationEnabled;
        this.emailNotifications = emailNotifications;
        this.pushNotifications = pushNotifications;
        this.smsNotifications = smsNotifications;
        this.timezone = timezone;
        this.defaultLocationId = defaultLocationId;
    }

}
