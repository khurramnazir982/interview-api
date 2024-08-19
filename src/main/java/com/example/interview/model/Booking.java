package com.example.interview.model;

import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Booking {
    private Long id;
    private ConferenceRoom room;
    private LocalTime startTime;
    private LocalTime endTime;
    private int numberOfPeople;
}
