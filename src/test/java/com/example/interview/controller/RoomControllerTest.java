package com.example.interview.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import com.example.interview.model.ConferenceRoom;
import com.example.interview.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RoomControllerTest {

    @Mock
    private RoomService roomService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext) {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testInvalidStartTimeFormat() throws Exception {
        mockMvc.perform(get("/api/rooms/available")
                                .param("startTime", "09:134") // Invalid start time format
                                .param("endTime", "13:00")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid start time format. Use HH:mm format."));
    }

    @Test
    public void testInvalidEndTimeFormat() throws Exception {
        mockMvc.perform(get("/api/rooms/available")
                                .param("startTime", "09:00")
                                .param("endTime", "13:0") // Invalid end time format
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid end time format. Use HH:mm format."));
    }

    @Test
    public void testMissingStartTime() throws Exception {
        mockMvc.perform(get("/api/rooms/available")
                                .param("endTime", "13:00") // Missing start time
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request: Start time is required"));
    }

    @Test
    public void testMissingEndTime() throws Exception {
        mockMvc.perform(get("/api/rooms/available")
                                .param("startTime", "09:00") // Missing end time
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request: End time is required"));
    }

    @Test
    public void testValidTimeFormat() throws Exception {
        when(roomService.getAvailableRooms("10:00", "13:00")).thenReturn(null); // Mocking service method

        mockMvc.perform(get("/api/rooms/available")
                                .param("startTime", "10:00")
                                .param("endTime", "13:00")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testValidTimeFormatAndReturnRooms() throws Exception {
        List<ConferenceRoom> mockRooms = Arrays.asList(
                new ConferenceRoom("Amaze", 3, Arrays.asList(
                        new LocalTime[] {LocalTime.of(9, 0), LocalTime.of(9, 15)},
                        new LocalTime[] {LocalTime.of(13, 0), LocalTime.of(13, 15)},
                        new LocalTime[] {LocalTime.of(17, 0), LocalTime.of(17, 15)}
                )),
                new ConferenceRoom("Beauty", 7, Arrays.asList(
                        new LocalTime[] {LocalTime.of(9, 0), LocalTime.of(9, 15)},
                        new LocalTime[] {LocalTime.of(13, 0), LocalTime.of(13, 15)},
                        new LocalTime[] {LocalTime.of(17, 0), LocalTime.of(17, 15)}
                )),
                new ConferenceRoom("Inspire", 12, Arrays.asList(
                        new LocalTime[] {LocalTime.of(9, 0), LocalTime.of(9, 15)},
                        new LocalTime[] {LocalTime.of(13, 0), LocalTime.of(13, 15)},
                        new LocalTime[] {LocalTime.of(17, 0), LocalTime.of(17, 15)}
                )),
                new ConferenceRoom("Strive", 20, Arrays.asList(
                        new LocalTime[] {LocalTime.of(9, 0), LocalTime.of(9, 15)},
                        new LocalTime[] {LocalTime.of(13, 0), LocalTime.of(13, 15)},
                        new LocalTime[] {LocalTime.of(17, 0), LocalTime.of(17, 15)}
                ))
        );

        when(roomService.getAvailableRooms("10:00", "13:00")).thenReturn(mockRooms);

        mockMvc.perform(get("/api/rooms/available")
                                .param("startTime", "10:00")
                                .param("endTime", "13:00")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name", is("Amaze")))
                .andExpect(jsonPath("$[0].capacity", is(3)))
                .andExpect(jsonPath("$[0].maintenanceSchedule", hasSize(3)))
                .andExpect(jsonPath("$[0].maintenanceSchedule[0][0]", is("09:00:00")))
                .andExpect(jsonPath("$[0].maintenanceSchedule[0][1]", is("09:15:00")))
                .andExpect(jsonPath("$[0].maintenanceSchedule[1][0]", is("13:00:00")))
                .andExpect(jsonPath("$[0].maintenanceSchedule[1][1]", is("13:15:00")))
                .andExpect(jsonPath("$[0].maintenanceSchedule[2][0]", is("17:00:00")))
                .andExpect(jsonPath("$[0].maintenanceSchedule[2][1]", is("17:15:00")))
                .andExpect(jsonPath("$[1].name", is("Beauty")))
                .andExpect(jsonPath("$[1].capacity", is(7)))
                .andExpect(jsonPath("$[2].name", is("Inspire")))
                .andExpect(jsonPath("$[2].capacity", is(12)))
                .andExpect(jsonPath("$[3].name", is("Strive")))
                .andExpect(jsonPath("$[3].capacity", is(20)));
    }
}
