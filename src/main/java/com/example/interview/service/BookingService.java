package com.example.interview.service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.example.interview.dto.BookingRequest;
import com.example.interview.exception.AllRoomsBookedException;
import com.example.interview.exception.InvalidNumberOfPeopleException;
import com.example.interview.exception.InvalidTimeIntervalException;
import com.example.interview.exception.MaintenanceTimeException;
import com.example.interview.exception.NoRoomAvailableException;
import com.example.interview.model.Booking;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.repo.BookingRepository;
import com.example.interview.repo.ConferenceRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public String bookRoom(BookingRequest request) {
        LocalTime startTime;
        LocalTime endTime;

        try {
            startTime = LocalTime.parse(request.getStartTime());
            endTime = LocalTime.parse(request.getEndTime());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Please use HH:mm format (e.g., 14:30).");
        }

        int numberOfPeople = request.getNumberOfPeople();

        // Validate booking request
        if (numberOfPeople <= 1) {
            throw new InvalidNumberOfPeopleException("Number of people should be greater than 1.");
        }

        // Check if the time intervals are valid
        validateTimeInterval(startTime, endTime);

        // Validate booking duration
        validateBookingDuration(startTime, endTime);

        // Find the best available room
        Optional<ConferenceRoom> availableRoom = findAvailableRoom(startTime, endTime, numberOfPeople);

        if (availableRoom.isPresent()) {
            ConferenceRoom room = availableRoom.get();

            // Validate room object
            if (room == null) {
                throw new NoRoomAvailableException("No suitable room found.");
            }

            // Creating a Booking using Lombok's @Builder
            Booking booking = Booking.builder()
                    .room(room)
                    .startTime(startTime)
                    .endTime(endTime)
                    .numberOfPeople(numberOfPeople)
                    .build();

            // Save the booking
            if (bookingRepository == null) {
                throw new IllegalStateException("Booking repository is not initialized.");
            }
            bookingRepository.save(booking);

            return String.format("Room '%s' booked successfully for %d people from %s to %s.",
                                 room.getName(), numberOfPeople, startTime, endTime);
        } else {
            // Check for alternative rooms with lower capacity
            List<ConferenceRoom> lowerCapacityRooms = findLowerCapacityRooms(startTime, endTime, numberOfPeople);

            if (!lowerCapacityRooms.isEmpty()) {
                StringBuilder message = new StringBuilder("All rooms suitable for ")
                        .append(numberOfPeople)
                        .append(" people are booked, but the following rooms with lower capacity are available during the requested time:\n");

                lowerCapacityRooms.forEach(room ->
                                                   message.append(String.format("Room '%s' with a capacity of %d people.\n",
                                                                                room.getName(), room.getCapacity())));

                throw new NoRoomAvailableException(message.toString().trim());
            } else {
                throw new NoRoomAvailableException("No suitable room available for the requested time.");
            }
        }
    }

    private void validateTimeInterval(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null.");
        }
        if (startTime.equals(endTime) || startTime.isAfter(endTime)) {
            throw new InvalidTimeIntervalException("End time must be after start time.");
        }
        if (startTime.getMinute() % 15 != 0 || endTime.getMinute() % 15 != 0) {
            throw new InvalidTimeIntervalException("Booking times must be in 15-minute intervals.");
        }
    }

    private void validateBookingDuration(LocalTime startTime, LocalTime endTime) {
        Duration duration = Duration.between(startTime, endTime);

        // Ensure booking duration is at least 30 minutes
        if (duration.isNegative() || duration.isZero() || duration.toMinutes() < 30) {
            throw new InvalidTimeIntervalException("Booking duration must be at least 30 minutes.");
        }

        // Ensure booking duration does not exceed 4 hours
        if (duration.toHours() > 4 || (duration.toHours() == 4 && duration.toMinutesPart() > 0)) {
            throw new InvalidTimeIntervalException("Booking duration cannot exceed 4 hours.");
        }
    }


    private Optional<ConferenceRoom> findAvailableRoom(LocalTime startTime, LocalTime endTime, int numberOfPeople) {
        if (conferenceRoomRepository == null) {
            throw new IllegalStateException("Conference room repository is not initialized.");
        }

        List<ConferenceRoom> rooms = conferenceRoomRepository.findAll();
        if (rooms == null || rooms.isEmpty()) {
            throw new NoRoomAvailableException("No rooms available in the repository.");
        }

        // Check if all rooms are already booked
        boolean allRoomsBooked = rooms.stream()
                .noneMatch(room -> isRoomAvailable(room, startTime, endTime));

        if (allRoomsBooked) {
            throw new AllRoomsBookedException("All rooms are already booked during the requested time.");
        }

        // Filter rooms based on capacity first
        List<ConferenceRoom> availableRooms = rooms.stream()
                .filter(room -> room.getCapacity() >= numberOfPeople)
                .collect(Collectors.toList());

        // Filter out rooms that are already booked during the requested time
        availableRooms = availableRooms.stream()
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .collect(Collectors.toList());

        // Return the best room based on capacity
        return availableRooms.stream()
                .sorted(Comparator.comparingInt(ConferenceRoom::getCapacity)) // Sort by capacity for optimal booking
                .findFirst();
    }

    private List<ConferenceRoom> findLowerCapacityRooms(LocalTime startTime, LocalTime endTime, int numberOfPeople) {
        if (conferenceRoomRepository == null) {
            throw new IllegalStateException("Conference room repository is not initialized.");
        }

        List<ConferenceRoom> rooms = conferenceRoomRepository.findAll();
        if (rooms == null || rooms.isEmpty()) {
            throw new NoRoomAvailableException("No rooms available in the repository.");
        }

        // Filter rooms with lower capacity than needed
        return rooms.stream()
                .filter(room -> room.getCapacity() < numberOfPeople) // Rooms with lower capacity
                .filter(room -> isRoomAvailable(room, startTime, endTime)) // Available during the requested time
                .sorted(Comparator.comparingInt(ConferenceRoom::getCapacity).reversed()) // Sort by capacity descending
                .collect(Collectors.toList());
    }

    private boolean isRoomAvailable(ConferenceRoom room, LocalTime startTime, LocalTime endTime) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }

        if (bookingRepository == null) {
            throw new IllegalStateException("Booking repository is not initialized.");
        }

        // Check for existing bookings
        List<Booking> existingBookings = bookingRepository.findByRoom(room);
        for (Booking booking : existingBookings) {
            // Allow bookings that start exactly when another booking ends, or end exactly when another booking starts
            if ((startTime.isBefore(booking.getEndTime()) && !startTime.equals(booking.getEndTime())) &&
                    (endTime.isAfter(booking.getStartTime()) && !endTime.equals(booking.getStartTime()))) {
                return false; // Overlapping booking
            }
        }

        // Check maintenance schedule
        for (LocalTime[] maintenanceSlot : room.getMaintenanceSchedule()) {
            // Allow bookings that start exactly when maintenance ends, or end exactly when maintenance starts
            if ((startTime.isBefore(maintenanceSlot[1]) && !startTime.equals(maintenanceSlot[1])) &&
                    (endTime.isAfter(maintenanceSlot[0]) && !endTime.equals(maintenanceSlot[0]))) {
                StringBuilder message = new StringBuilder("The requested time overlaps with the following maintenance windows for room: ")
                        .append(room.getName()).append(": ")
                        .append(String.format("[%s to %s]", maintenanceSlot[0], maintenanceSlot[1]));
                throw new MaintenanceTimeException(message.toString().trim());
            }
        }

        return true; // Room is available
    }
}
