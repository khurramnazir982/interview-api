package com.example.interview.controller;

import com.example.interview.dto.BookingRequest;
import com.example.interview.exception.AllRoomsBookedException;
import com.example.interview.exception.BookingNotFoundException;
import com.example.interview.exception.InvalidNumberOfPeopleException;
import com.example.interview.exception.InvalidTimeIntervalException;
import com.example.interview.exception.MaintenanceTimeException;
import com.example.interview.exception.NoRoomAvailableException;
import com.example.interview.model.Booking;
import com.example.interview.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable String id) {
        // Validate the ID
        ResponseEntity<String> validationResponse = validateId(id);
        if (validationResponse != null) {
            return validationResponse;
        }

        long bookingId = Long.parseLong(id);

        try {
            bookingService.deleteBooking(bookingId);
            return ResponseEntity.ok("Booking deleted successfully.");
        } catch (BookingNotFoundException e) {
            log.error("Booking deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred while deleting booking: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> viewBookingDetails(@PathVariable String id) {
        ResponseEntity<String> validationResponse = validateId(id);
        if (validationResponse != null) {
            return validationResponse;
        }

        Long bookingId = Long.parseLong(id);

        log.info("GET /api/bookings/view/{} called", bookingId);

        try {
            Booking booking = bookingService.getBookingById(bookingId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(booking);
        } catch (BookingNotFoundException e) {
            log.error("Booking not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching booking: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }

    private ResponseEntity<String> validateId(String id) {
        if (id == null || !id.matches("^[0-9]+$")) {
            log.error("Invalid ID: {}", id);
            return ResponseEntity.badRequest().body("Invalid ID. ID must be a positive integer.");
        }

        try {
            Long.parseLong(id);
        } catch (NumberFormatException e) {
            log.error("ID is not a valid number: {}", id);
            return ResponseEntity.badRequest().body("Invalid ID format. ID must be a positive integer.");
        }

        return null;
    }
}
