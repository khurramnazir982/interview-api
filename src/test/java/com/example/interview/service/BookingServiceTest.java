package com.example.interview.service;

import static com.example.interview.utils.TestConstants.AMAZE_1100_1200_REQUEST;
import static com.example.interview.utils.TestConstants.AMAZE_ROOM_NAME;
import static com.example.interview.utils.TestConstants.BEAUTY_ROOM_NAME;
import static com.example.interview.utils.TestConstants.STRIVE_1100_1200_REQUEST;
import static com.example.interview.utils.TestConstants.STRIVE_ROOM_NAME;
import static com.example.interview.utils.TestConstants.TIME_09_15;
import static com.example.interview.utils.TestConstants.TIME_09_30;
import static com.example.interview.utils.TestConstants.TIME_10_00;
import static com.example.interview.utils.TestConstants.TIME_10_30;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import com.example.interview.dto.BookingRequest;
import com.example.interview.exception.AllRoomsBookedException;
import com.example.interview.exception.BookingNotFoundException;
import com.example.interview.exception.InvalidNumberOfPeopleException;
import com.example.interview.exception.InvalidTimeIntervalException;
import com.example.interview.exception.MaintenanceTimeException;
import com.example.interview.exception.NoRoomAvailableException;
import com.example.interview.model.Booking;
import com.example.interview.repo.BookingRepository;
import com.example.interview.repo.ConferenceRoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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

    @AfterEach
    public void tearDown() {
        bookingRepository.clear();
    }

    static Stream<Arguments> roomBookingParameters() {
        return Stream.of(
                Arguments.of(3, "Amaze", "Room 'Amaze' booked successfully for 3 people from 09:30 to 10:00."),
                Arguments.of(7, "Beauty", "Room 'Beauty' booked successfully for 7 people from 09:30 to 10:00."),
                Arguments.of(12, "Inspire", "Room 'Inspire' booked successfully for 12 people from 09:30 to 10:00."),
                Arguments.of(20, "Strive", "Room 'Strive' booked successfully for 20 people from 09:30 to 10:00.")
        );
    }

    @ParameterizedTest(name = "{index}: Number of People - {0} - should do a successful booking in {1} Room.")
    @MethodSource("roomBookingParameters")
    @DisplayName("Test Successful Booking for Various Room Names and Capacities")
    public void testBookRoom_SuccessfulBooking(int numberOfPeople, String roomName, String expectedMessage) {
        assertBooking(roomName, TIME_09_30, TIME_10_00, numberOfPeople, expectedMessage);
    }

    @ParameterizedTest(name = "{index}: Invalid booking with startTime={0}, endTime={1} should throw an exception with message={2}")
    @CsvSource({
            "10:00, 09:30, End time must be after start time.",
            "09:07, 09:30, Booking times must be in 15-minute intervals.",
            "09:15, 09:30, Booking duration must be at least 30 minutes.",
            "17:15, 23:30, Booking duration cannot exceed 5 hours."
    })
    public void testBookRoom_InvalidBooking_bookingUnsuccessful(String startTime, String endTime, String expectedMessage) {
        assertInvalidTimeIntervalException(
                bookingService, startTime, endTime, expectedMessage);
    }

    @Test
    public void testBookRoom_SuccessfulBooking_timeOnBorderMaintenanceWindow() {
        // 09:15 is the maintenance window end time
        assertBooking(BEAUTY_ROOM_NAME, TIME_09_15, TIME_10_00, 5,
                      "Room 'Beauty' booked successfully for 5 people from 09:15 to 10:00.");
    }

    @Test
    public void testBookRoom_SuccessfulBooking_optimizedRoomSelected() {
        assertBooking(STRIVE_ROOM_NAME, TIME_09_30, TIME_10_30, 13,
                      "Room 'Strive' booked successfully for 13 people from 09:30 to 10:30.");
    }

    @Test
    public void testBookRoom_MaxCapacity() {
        assertBooking(STRIVE_ROOM_NAME, TIME_09_30, TIME_10_30, 20,
                      "Room 'Strive' booked successfully for 20 people from 09:30 to 10:30.");
    }

    @Test
    public void testBookRoom_MinCapacity() {
        assertBooking(AMAZE_ROOM_NAME, TIME_09_30, TIME_10_30, 2,
                      "Room 'Amaze' booked successfully for 2 people from 09:30 to 10:30.");
    }

    @Test
    public void testBookRoom_malformedStartTime_bookingUnsuccessful() {
        BookingRequest request = BookingRequest.builder()
                .startTime("09:154")
                .endTime("09:30")
                .numberOfPeople(5)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.bookRoom(request)
        );

        assertEquals("Invalid start time format. Please use HH:mm format (e.g., 14:30).", exception.getMessage());
    }

    @Test
    public void testBookRoom_malformedEndTime_bookingUnsuccessful() {
        BookingRequest request = BookingRequest.builder()
                .startTime("09:15")
                .endTime("09:303")
                .numberOfPeople(5)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.bookRoom(request)
        );

        assertEquals("Invalid end time format. Please use HH:mm format (e.g., 14:30).", exception.getMessage());
    }

    @Test
    public void testBookRoom_allRoomsBookedException_bookingUnsuccessful() {
        bookingService.bookRoom(AMAZE_1100_1200_REQUEST);
        bookingService.bookRoom(AMAZE_1100_1200_REQUEST);
        bookingService.bookRoom(AMAZE_1100_1200_REQUEST);
        bookingService.bookRoom(AMAZE_1100_1200_REQUEST);

        AllRoomsBookedException exception = assertThrows(
                AllRoomsBookedException.class,
                () -> bookingService.bookRoom(STRIVE_1100_1200_REQUEST)
        );

        assertEquals("All rooms are already booked during the requested time.", exception.getMessage());
    }

    @Test
    public void testBookRoom_noRoomAvailableException_lowerCapacityAvailable_bookingUnsuccessful() {
        bookingService.bookRoom(STRIVE_1100_1200_REQUEST);
        BookingRequest request = BookingRequest.builder()
                .startTime("11:00")
                .endTime("12:00")
                .numberOfPeople(20)
                .build();

        NoRoomAvailableException exception = assertThrows(
                NoRoomAvailableException.class,
                () -> bookingService.bookRoom(request)
        );

        assertEquals(
                "All rooms suitable for 20 people are booked, but the following rooms with lower capacity are available during the requested time:\n" +
                        "Room 'Inspire' with a capacity of 12 people.\n" +
                        "Room 'Beauty' with a capacity of 7 people.\n" +
                        "Room 'Amaze' with a capacity of 3 people.",
                exception.getMessage());
    }


    @Test
    public void testBookRoom_maintenanceTimeException_bookingUnsuccessful() {
        BookingRequest request = BookingRequest.builder()
                .startTime("12:00")
                .endTime("13:15")
                .numberOfPeople(5)
                .build();

        MaintenanceTimeException exception = assertThrows(
                MaintenanceTimeException.class,
                () -> bookingService.bookRoom(request)
        );

        assertEquals("The requested time overlaps with the following maintenance windows for room: Amaze: [13:00 to 13:15]",
                     exception.getMessage());
    }

    @Test
    public void testBookRoom_2MaintenanceWindowOverlaps_shouldThrowMaintenanceWindowTimeException_bookingUnsuccessful() {
        BookingRequest request = BookingRequest.builder()
                .startTime("09:00")
                .endTime("13:15")
                .numberOfPeople(5)
                .build();

        MaintenanceTimeException exception = assertThrows(
                MaintenanceTimeException.class,
                () -> bookingService.bookRoom(request)
        );

        assertEquals("The requested time overlaps with the following maintenance windows for room: Amaze: [09:00 to 09:15] [13:00 to 13:15]",
                     exception.getMessage());
    }

    @Test
    public void testBookRoom_invalidNumberOfPeopleException_bookingUnsuccessful() {
        BookingRequest request = BookingRequest.builder()
                .startTime("09:00")
                .endTime("10:00")
                .numberOfPeople(1)
                .build();

        InvalidNumberOfPeopleException exception = assertThrows(
                InvalidNumberOfPeopleException.class,
                () -> bookingService.bookRoom(request)
        );

        assertEquals("Number of people should be greater than 1.", exception.getMessage());
    }

    @Test
    public void testDeleteBooking_bookingInRepo_SuccessfulDeletion() {
        String bookingConfirmation = bookingService.bookRoom(AMAZE_1100_1200_REQUEST);

        assertNotNull(bookingConfirmation);
        assertEquals("Room 'Amaze' booked successfully for 3 people from 11:00 to 12:00.", bookingConfirmation);

        // Verify the booking is saved in the repository
        List<Booking> bookings = bookingRepository.findByRoomAndTime(
                conferenceRoomRepository.findByName("Amaze").orElseThrow(),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );
        assertEquals(1, bookings.size());

        Booking savedBooking = bookings.get(0);
        Long bookingId = savedBooking.getId();

        bookingService.deleteBooking(bookingId);

        bookings = bookingRepository.findByRoomAndTime(
                conferenceRoomRepository.findByName("Amaze").orElseThrow(),
                LocalTime.of(11, 00),
                LocalTime.of(12, 0)
        );

        assertEquals(0, bookings.size());
    }

    @Test
    public void testDeleteBooking_BookingNotFound_shouldThrowBookingNotFoundException_bookingUnsuccessful() {
        Long nonExistentBookingId = 999L;

        BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.deleteBooking(nonExistentBookingId)
        );

        assertEquals("Booking with ID " + nonExistentBookingId + " not found.", exception.getMessage());
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

    public void assertInvalidTimeIntervalException(
            BookingService bookingService,
            String startTime,
            String endTime,
            String expectedMessage) {

        BookingRequest request = BookingRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .numberOfPeople(5)
                .build();

        InvalidTimeIntervalException exception = assertThrows(
                InvalidTimeIntervalException.class,
                () -> bookingService.bookRoom(request)
        );

        assertEquals(expectedMessage, exception.getMessage());
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