package com.example.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomAvailabilityRequest {

    @NotBlank(message = "Bad Request: Start time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid start time format. Use HH:mm format.")
    private String startTime;

    @NotBlank(message = "Bad Request: End time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid end time format. Use HH:mm format.")
    private String endTime;

}
