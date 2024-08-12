package com.example.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingRequest {

    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid start time format. Use HH:mm format.")
    private String startTime;

    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid end time format. Use HH:mm format.")
    private String endTime;

    @NotNull(message = "Number of people is required")
    @Min(value = 2, message = "Number of people should be greater than 1")
    @Max(value = 20, message = "Number of people should not exceed the maximum room capacity")
    private Integer numberOfPeople;
}
