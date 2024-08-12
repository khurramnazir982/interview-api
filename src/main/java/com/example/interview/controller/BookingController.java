package com.example.interview.controller;

import java.util.List;

import com.example.interview.dto.BookingRequest;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.service.BookingService;
import com.example.interview.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @PostMapping("/book")
    public ResponseEntity<String> bookRoom(@Valid @RequestBody BookingRequest bookingRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError().getDefaultMessage());
        }

        try {
            String message = bookingService.bookRoom(bookingRequest);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<ConferenceRoom>> getAvailableRooms(@RequestParam String startTime, @RequestParam String endTime) {
        List<ConferenceRoom> availableRooms = roomService.getAvailableRooms(startTime, endTime);
        return ResponseEntity.ok(availableRooms);
    }
}