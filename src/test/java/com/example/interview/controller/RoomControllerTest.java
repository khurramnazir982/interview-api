//package com.example.interview.controller;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import org.junit.jupiter.api.Test;
//
//import com.example.interview.dto.RoomAvailabilityRequest;
//import com.example.interview.service.RoomService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(RoomController.class)
//@ExtendWith(MockitoExtension.class)
//public class RoomControllerTest {
//
//    @Mock
//    private RoomService roomService;
//
//    @InjectMocks
//    private RoomController roomController;
//
//    private MockMvc mockMvc;
//
//    @BeforeEach
//    public void setUp(WebApplicationContext webApplicationContext) {
//        MockitoAnnotations.openMocks(this);
//        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//    }
//
//    @Test
//    public void testInvalidStartTimeFormat() throws Exception {
//        mockMvc.perform(get("/api/rooms/available")
//                                .param("startTime", "09:134") // Invalid start time format
//                                .param("endTime", "13:00")
//                                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Invalid start time format. Use HH:mm format."));
//    }
//
//    @Test
//    public void testInvalidEndTimeFormat() throws Exception {
//        mockMvc.perform(get("/api/rooms/available")
//                                .param("startTime", "09:00")
//                                .param("endTime", "13:0") // Invalid end time format
//                                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Invalid end time format. Use HH:mm format."));
//    }
//
//    @Test
//    public void testMissingStartTime() throws Exception {
//        mockMvc.perform(get("/api/rooms/available")
//                                .param("endTime", "13:00") // Missing start time
//                                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Bad Request: Start time is required"));
//    }
//
//    @Test
//    public void testMissingEndTime() throws Exception {
//        mockMvc.perform(get("/api/rooms/available")
//                                .param("startTime", "09:00") // Missing end time
//                                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Bad Request: End time is required"));
//    }
//
//    @Test
//    public void testValidTimeFormat() throws Exception {
//        when(roomService.getAvailableRooms("09:00", "13:00")).thenReturn(null); // Mocking service method
//
//        mockMvc.perform(get("/api/rooms/available")
//                                .param("startTime", "09:00")
//                                .param("endTime", "13:00")
//                                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//}
