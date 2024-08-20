package com.example.interview.controller;

import static com.example.interview.utils.TestConstants.AMAZE_1100_1200_REQUEST;
import static com.example.interview.utils.TestConstants.AMAZE_ROOM_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.Optional;

import com.example.interview.dto.BookingRequest;
import com.example.interview.exception.AllRoomsBookedException;
import com.example.interview.exception.BookingNotFoundException;
import com.example.interview.exception.InvalidNumberOfPeopleException;
import com.example.interview.exception.InvalidTimeIntervalException;
import com.example.interview.exception.MaintenanceTimeException;
import com.example.interview.model.Booking;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.repo.ConferenceRoomRepository;
import com.example.interview.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @MockBean
    private BookingService bookingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSuccessfulBooking() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .startTime("09:30")
                .endTime("10:00")
                .numberOfPeople(5)
                .build();

        when(bookingService.bookRoom(any(BookingRequest.class)))
                .thenReturn("Room 'Beauty' booked successfully for 5 people from 09:30 to 10:00.");

        mockMvc.perform(post("/api/bookings/book")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(bookingRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Room 'Beauty' booked successfully for 5 people from 09:30 to 10:00."));
    }

    @Test
    public void testInvalidNumberOfPeopleException() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .startTime("09:30")
                .endTime("10:00")
                .numberOfPeople(0)
                .build();

        when(bookingService.bookRoom(any(BookingRequest.class)))
                .thenThrow(new InvalidNumberOfPeopleException("Number of people should be greater than 1"));

        mockMvc.perform(post("/api/bookings/book")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(bookingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Number of people should be greater than 1"));
    }

    @Test
    public void testInvalidTimeIntervalException() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .startTime("10:00")
                .endTime("09:30")
                .numberOfPeople(5)
                .build();

        when(bookingService.bookRoom(any(BookingRequest.class)))
                .thenThrow(new InvalidTimeIntervalException("End time must be after start time."));

        mockMvc.perform(post("/api/bookings/book")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(bookingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("End time must be after start time."));
    }

    @Test
    public void testMaintenanceTimeException() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .startTime("13:00")
                .endTime("14:00")
                .numberOfPeople(5)
                .build();

        when(bookingService.bookRoom(any(BookingRequest.class)))
                .thenThrow(new MaintenanceTimeException(
                        "The requested time overlaps with the following maintenance windows for room: Amaze: [13:00 to 13:15]"));

        mockMvc.perform(post("/api/bookings/book")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(bookingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "The requested time overlaps with the following maintenance windows for room: Amaze: [13:00 to 13:15]"));
    }

    @Test
    public void testAllRoomsBookedException() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .startTime("10:00")
                .endTime("11:30")
                .numberOfPeople(5)
                .build();

        // Mock service to throw AllRoomsBookedException
        when(bookingService.bookRoom(any(BookingRequest.class)))
                .thenThrow(new AllRoomsBookedException("All rooms are fully booked for the requested time."));

        mockMvc.perform(post("/api/bookings/book")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(bookingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("All rooms are fully booked for the requested time."));
    }

    @Test
    public void testUnexpectedError() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .startTime("10:00")
                .endTime("11:30")
                .numberOfPeople(5)
                .build();

        when(bookingService.bookRoom(any(BookingRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error occurred."));

        mockMvc.perform(post("/api/bookings/book")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(bookingRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred."));
    }

    @Test
    void testDeleteBooking_BookingNotFound() throws Exception {
        Long bookingId = 1L;

        doThrow(new BookingNotFoundException("Booking with ID " + bookingId + " not found."))
                .when(bookingService).deleteBooking(bookingId);

        mockMvc.perform(delete("/api/bookings/delete/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Booking with ID " + bookingId + " not found."));

        verify(bookingService).deleteBooking(bookingId);
    }

    @ParameterizedTest(name = "{index}: Invalid ID: {0} - should throw bad request exception")
    @ValueSource(strings = {"id", "-1"})
    void testDeleteBooking_InvalidBookingId_ShouldReturnBadRequest(String bookingId) throws Exception {
        mockMvc.perform(delete("/api/bookings/delete/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid ID. ID must be a positive integer."));
    }

    @Test
    void testDeleteBooking_Success() throws Exception {
        Long bookingId = 1L;
        mockMvc.perform(delete("/api/bookings/delete/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking deleted successfully."));

        verify(bookingService).deleteBooking(bookingId);
    }

    @Test
    void testViewBooking_Success() throws Exception {
        ConferenceRoom room = conferenceRoomRepository.findByName(AMAZE_ROOM_NAME).orElseThrow();
        Booking expectedBooking = Booking.builder()
                .id(1L)
                .startTime(LocalTime.of(11, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .numberOfPeople(3)
                .room(room)
                .build();

        when(bookingService.getBookingById(1L)).thenReturn(expectedBooking);

        Long bookingId = 1L;
        String expectedJson = "{"
                + "\"id\":1,"
                + "\"startTime\":\"11:00:00\","
                + "\"endTime\":\"12:00:00\","
                + "\"numberOfPeople\":3,"
                + "\"room\":{"
                + "\"name\":\"Amaze\","
                + "\"capacity\":3,"
                + "\"maintenanceSchedule\":["
                + "[\"09:00:00\",\"09:15:00\"],"
                + "[\"13:00:00\",\"13:15:00\"],"
                + "[\"17:00:00\",\"17:15:00\"]"
                + "]"
                + "}"
                + "}";

        mockMvc.perform(get("/api/bookings/view/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson))
                .andReturn();

        verify(bookingService).getBookingById(bookingId);
    }

    @ParameterizedTest(name = "{index}: Invalid ID: {0} - should throw bad request exception")
    @ValueSource(strings = {"id", "-1"})
    void testViewBooking_InvalidBookingId_ShouldReturnBadRequest(String bookingId) throws Exception {
        mockMvc.perform(get("/api/bookings/view/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid ID. ID must be a positive integer."));
    }
}
