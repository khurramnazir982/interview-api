package com.example.interview.controller;

import java.util.List;

import com.example.interview.dto.RoomAvailabilityRequest;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRooms(@Valid RoomAvailabilityRequest availabilityRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError().getDefaultMessage());
        }

        try {
            List<ConferenceRoom> availableRooms = roomService.getAvailableRooms(availabilityRequest.getStartTime(), availabilityRequest.getEndTime());
            return ResponseEntity.ok(availableRooms);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

}
