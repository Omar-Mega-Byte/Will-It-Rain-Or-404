package com.weather_found.weather_app.modules.user.dto.response;

/**
 * DTO for returning user preferences
 */
public class UserPreferencesResponseDto {

    private String temperatureUnit;
    private Boolean notificationEnabled;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean smsNotifications;
    private String timezone;
    private Long defaultLocationId;

    public UserPreferencesResponseDto() {}

    public UserPreferencesResponseDto(String temperatureUnit, Boolean notificationEnabled,
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

    // Getters and setters
    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public Boolean getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getPushNotifications() {
        return pushNotifications;
    }

    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }

    public Boolean getSmsNotifications() {
        return smsNotifications;
    }

    public void setSmsNotifications(Boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Long getDefaultLocationId() {
        return defaultLocationId;
    }

    public void setDefaultLocationId(Long defaultLocationId) {
        this.defaultLocationId = defaultLocationId;
    }
}
