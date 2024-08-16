package com.example.interview.controller;

import com.example.interview.dto.BookingRequest;
import com.example.interview.exception.AllRoomsBookedException;
import com.example.interview.exception.InvalidNumberOfPeopleException;
import com.example.interview.exception.InvalidTimeIntervalException;
import com.example.interview.exception.MaintenanceTimeException;
import com.example.interview.exception.NoRoomAvailableException;
import com.example.interview.service.BookingService;
import com.example.interview.service.RoomService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @PostMapping("/book")
    public ResponseEntity<String> bookRoom(@Valid @RequestBody BookingRequest bookingRequest, BindingResult result) {
        log.info("POST /api/bookings/book called");
        log.trace("POST /api/bookings/book request={}", bookingRequest);

        if (bookingRequest == null) {
            log.error("Booking request is null.");
            throw new IllegalArgumentException("Booking request cannot be null.");
        }

        if (result.hasErrors()) {
            String errorMessage = result.getFieldError().getDefaultMessage();
            log.error("Validation failed: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }

        try {
            log.info("Processing booking request: {}", bookingRequest);
            String message = bookingService.bookRoom(bookingRequest);
            log.info("Booking successful: {}", message);
            return ResponseEntity.ok(message);
        } catch (InvalidNumberOfPeopleException | MaintenanceTimeException | NoRoomAvailableException
                 | InvalidTimeIntervalException | AllRoomsBookedException e) {
            log.error("Booking failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }
}
