package com.example.interview.controller;

import java.util.List;

import com.example.interview.dto.RoomAvailabilityRequest;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.service.RoomService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRooms(@Valid RoomAvailabilityRequest availabilityRequest, BindingResult result) {
        log.info("GET /api/rooms/available called");
        log.trace("GET /api/rooms/available request={}", availabilityRequest);

        if (result.hasErrors()) {
            String errorMessage = result.getFieldError().getDefaultMessage();
            log.error("Validation failed: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }

        try {
            log.info("Processing room availability request: {}", availabilityRequest);
            List<ConferenceRoom> availableRooms = roomService.getAvailableRooms(availabilityRequest.getStartTime(),
                                                                                availabilityRequest.getEndTime());
            log.info("Room availability check successful, found {} rooms", availableRooms.size());
            return ResponseEntity.ok(availableRooms);
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

}
