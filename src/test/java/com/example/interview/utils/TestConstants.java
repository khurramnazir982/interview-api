package com.example.interview.utils;

import java.time.LocalTime;

import com.example.interview.dto.BookingRequest;

public class TestConstants {
    public static final String AMAZE_ROOM_NAME = "Amaze";
    public static final String BEAUTY_ROOM_NAME = "Beauty";
    public static final String INSPIRE_ROOM_NAME = "Inspire";
    public static final String STRIVE_ROOM_NAME = "Strive";
    public static final LocalTime TIME_09_15 = LocalTime.of(9, 15);
    public static final LocalTime TIME_10_00 = LocalTime.of(10, 0);
    public static final LocalTime TIME_09_30 = LocalTime.of(9, 30);
    public static final LocalTime TIME_10_30 = LocalTime.of(10, 30);

    public static final BookingRequest AMAZE_1100_1200_REQUEST = BookingRequest.builder()
            .startTime("11:00")
            .endTime("12:00")
            .numberOfPeople(3)
            .build();

    public static final BookingRequest STRIVE_1100_1200_REQUEST = BookingRequest.builder()
            .startTime("11:00")
            .endTime("12:00")
            .numberOfPeople(20)
            .build();


}
