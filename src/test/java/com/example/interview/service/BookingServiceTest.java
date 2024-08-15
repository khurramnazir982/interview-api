package com.example.interview.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import com.example.interview.dto.BookingRequest;
import com.example.interview.model.Booking;
import com.example.interview.repo.BookingRepository;
import com.example.interview.repo.ConferenceRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookingServiceTest {

    private static final String BEAUTY_ROOM_NAME = "Beauty";
    private static final String STRIVE_ROOM_NAME = "Strive";
    private static final LocalTime TIME_09_15 = LocalTime.of(9, 15);
    private static final LocalTime TIME_10_00 = LocalTime.of(10, 0);
    private static final LocalTime TIME_09_30 = LocalTime.of(9, 30);
    private static final LocalTime TIME_10_30 = LocalTime.of(10, 30);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @Autowired
    private BookingRepository bookingRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    static Stream<Arguments> roomBookingParameters() {
        return Stream.of(
                Arguments.of(3, "Amaze", "Room 'Amaze' booked successfully for 3 people from 09:30 to 10:00."),
                Arguments.of(7, "Beauty", "Room 'Beauty' booked successfully for 7 people from 09:30 to 10:00."),
                Arguments.of(12, "Inspire", "Room 'Inspire' booked successfully for 12 people from 09:30 to 10:00."),
                Arguments.of(20, "Strive", "Room 'Strive' booked successfully for 20 people from 09:30 to 10:00.")
        );
    }

    @ParameterizedTest(name = "{index} => Room: {1}, Number of People: {0}")
    @MethodSource("roomBookingParameters")
    @DisplayName("Test Successful Booking for Various Room Names and Capacities")
    public void testBookRoom_SuccessfulBooking(int numberOfPeople, String roomName, String expectedMessage) {
        assertBooking(roomName, TIME_09_30, TIME_10_00, numberOfPeople, expectedMessage);
    }

    @Test
    public void testBookRoom_SuccessfulBooking_timeOnBorderMaintenanceWindow() {
        assertBooking(BEAUTY_ROOM_NAME, TIME_09_15, TIME_10_00, 5,
                      "Room 'Beauty' booked successfully for 5 people from 09:15 to 10:00.");
    }

    @Test
    public void testBookRoom_SuccessfulBooking_optimizedRoomSelected() {
        assertBooking(STRIVE_ROOM_NAME, TIME_09_30, TIME_10_30, 13,
                      "Room 'Strive' booked successfully for 13 people from 09:30 to 10:30.");
    }

    private void assertBooking(String roomName, LocalTime startTime, LocalTime endTime, int numberOfPeople, String expectedMessage) {
        BookingRequest request = BookingRequest.builder()
                .startTime(startTime.toString())
                .endTime(endTime.toString())
                .numberOfPeople(numberOfPeople)
                .build();

        String result = bookingService.bookRoom(request);

        assertNotNull(result);
        assertEquals(expectedMessage, result);

        List<Booking> bookings = bookingRepository.findByRoomAndTime(
                conferenceRoomRepository.findByName(roomName).orElseThrow(),
                startTime,
                endTime
        );

        assertNotNull(bookings);
        assertEquals(1, bookings.size());

        Booking savedBooking = bookings.get(0);
        assertEquals(roomName, savedBooking.getRoom().getName());
        assertEquals(startTime, savedBooking.getStartTime());
        assertEquals(endTime, savedBooking.getEndTime());
        assertEquals(numberOfPeople, savedBooking.getNumberOfPeople());
    }

    @Test
    public void testBookRoom_SuccessfulBooking() {
        BookingRequest request = BookingRequest.builder()
                .startTime("09:30")
                .endTime("10:00")
                .numberOfPeople(5)
                .build();

        String result = bookingService.bookRoom(request);

        assertNotNull(result);
        assertEquals("Room 'Beauty' booked successfully for 5 people from 09:30 to 10:00.", result);

        // Verify that the booking is saved in the repository
        List<Booking> bookings = bookingRepository.findByRoomAndTime(
                conferenceRoomRepository.findByName("Beauty").orElseThrow(),
                LocalTime.of(9, 30),
                LocalTime.of(10, 0)
        );

        assertNotNull(bookings);
        assertEquals(1, bookings.size());

        Booking savedBooking = bookings.get(0);
        assertEquals("Beauty", savedBooking.getRoom().getName());
        assertEquals(LocalTime.of(9, 30), savedBooking.getStartTime());
        assertEquals(LocalTime.of(10, 0), savedBooking.getEndTime());
        assertEquals(5, savedBooking.getNumberOfPeople());
    }

}


