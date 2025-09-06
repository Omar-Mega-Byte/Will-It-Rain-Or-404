package com.weather_found.weather_app.modules.user.dto.response;

/**
 * Generic response DTO for simple messages
 */
public class MessageResponseDto {

    private String message;
    private boolean success;

    public MessageResponseDto() {}

    public MessageResponseDto(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public static MessageResponseDto success(String message) {
        return new MessageResponseDto(message, true);
    }

    public static MessageResponseDto error(String message) {
        return new MessageResponseDto(message, false);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
