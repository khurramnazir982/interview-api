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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookingServiceTest {

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
        // Arrange
        BookingRequest request = BookingRequest.builder()
                .startTime("09:30")
                .endTime("10:00")
                .numberOfPeople(numberOfPeople)
                .build();

        // Act
        String result = bookingService.bookRoom(request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedMessage, result);

        // Verify that the booking is saved in the repository
        List<Booking> bookings = bookingRepository.findByRoomAndTime(
                conferenceRoomRepository.findByName(roomName).orElseThrow(),
                LocalTime.of(9, 30),
                LocalTime.of(10, 0)
        );

        assertNotNull(bookings);
        assertEquals(1, bookings.size());

        Booking savedBooking = bookings.get(0);
        assertEquals(roomName, savedBooking.getRoom().getName());
        assertEquals(LocalTime.of(9, 30), savedBooking.getStartTime());
        assertEquals(LocalTime.of(10, 0), savedBooking.getEndTime());
        assertEquals(numberOfPeople, savedBooking.getNumberOfPeople());
    }

}


