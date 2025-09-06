package com.weather_found.weather_app.modules.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {

    private String message;

    private Boolean success;

    public static MessageResponseDto success(String message) {
        return new MessageResponseDto(message, true);
    }

    public static MessageResponseDto error(String message) {
        return new MessageResponseDto(message, false);
    }
}
