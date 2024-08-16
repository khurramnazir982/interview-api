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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public String bookRoom(BookingRequest request) {
        log.info("bookRoom called with request: {}", request);

        LocalTime startTime;
        LocalTime endTime;

        try {
            startTime = LocalTime.parse(request.getStartTime());
            endTime = LocalTime.parse(request.getEndTime());
            log.debug("Parsed startTime: {}, endTime: {}", startTime, endTime);
        } catch (DateTimeParseException e) {
            log.error("Invalid time format in request: {}", request, e);
            throw new IllegalArgumentException("Invalid time format. Please use HH:mm format (e.g., 14:30).");
        }

        int numberOfPeople = request.getNumberOfPeople();

        // Validate booking request
        if (numberOfPeople <= 1) {
            log.error("Invalid number of people: {} in request: {}", numberOfPeople, request);
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
            log.info("Booking room: {} for {} people from {} to {}", room.getName(), numberOfPeople, startTime, endTime);

            Booking booking = Booking.builder()
                    .room(room)
                    .startTime(startTime)
                    .endTime(endTime)
                    .numberOfPeople(numberOfPeople)
                    .build();

            bookingRepository.save(booking);
            log.info("Room '{}' booked successfully", room.getName());

            return String.format("Room '%s' booked successfully for %d people from %s to %s.",
                                 room.getName(), numberOfPeople, startTime, endTime);
        } else {
            log.warn("No room available for {} people from {} to {}", numberOfPeople, startTime, endTime);

            List<ConferenceRoom> lowerCapacityRooms = findLowerCapacityRooms(startTime, endTime, numberOfPeople);

            if (!lowerCapacityRooms.isEmpty()) {
                StringBuilder message = new StringBuilder("All rooms suitable for ")
                        .append(numberOfPeople)
                        .append(" people are booked, but the following rooms with lower capacity are available during the requested time:\n");

                lowerCapacityRooms.forEach(room ->
                                                   message.append(String.format("Room '%s' with a capacity of %d people.\n",
                                                                                room.getName(), room.getCapacity())));

                log.warn("Rooms with lower capacity found: {}", lowerCapacityRooms);
                throw new NoRoomAvailableException(message.toString().trim());
            } else {
                log.error("No suitable room available for the requested time");
                throw new NoRoomAvailableException("No suitable room available for the requested time.");
            }
        }
    }

    private void validateTimeInterval(LocalTime startTime, LocalTime endTime) {
        log.debug("Validating time interval: {} to {}", startTime, endTime);
        if (startTime == null || endTime == null) {
            log.error("Start time or end time is null");
            throw new IllegalArgumentException("Start time and end time cannot be null.");
        }
        if (startTime.equals(endTime) || startTime.isAfter(endTime)) {
            log.error("Invalid time interval: startTime={} endTime={}", startTime, endTime);
            throw new InvalidTimeIntervalException("End time must be after start time.");
        }
        if (startTime.getMinute() % 15 != 0 || endTime.getMinute() % 15 != 0) {
            log.error("Time not in 15-minute intervals: startTime={} endTime={}", startTime, endTime);
            throw new InvalidTimeIntervalException("Booking times must be in 15-minute intervals.");
        }
    }

    private void validateBookingDuration(LocalTime startTime, LocalTime endTime) {
        log.debug("Validating booking duration from {} to {}", startTime, endTime);
        Duration duration = Duration.between(startTime, endTime);

        if (duration.isNegative() || duration.isZero() || duration.toMinutes() < 30) {
            log.error("Invalid booking duration: {} minutes", duration.toMinutes());
            throw new InvalidTimeIntervalException("Booking duration must be at least 30 minutes.");
        }

        if (duration.toHours() > 4 || (duration.toHours() == 4 && duration.toMinutesPart() > 0)) {
            log.error("Booking duration exceeds 4 hours: {} minutes", duration.toMinutes());
            throw new InvalidTimeIntervalException("Booking duration cannot exceed 4 hours.");
        }
    }

    private Optional<ConferenceRoom> findAvailableRoom(LocalTime startTime, LocalTime endTime, int numberOfPeople) {
        log.debug("Finding available room for {} people from {} to {}", numberOfPeople, startTime, endTime);
        List<ConferenceRoom> rooms = conferenceRoomRepository.findAll();

        if (rooms.isEmpty()) {
            log.error("No rooms available in the repository");
            throw new NoRoomAvailableException("No rooms available in the repository.");
        }

        boolean allRoomsBooked = rooms.stream()
                .noneMatch(room -> isRoomAvailable(room, startTime, endTime));

        if (allRoomsBooked) {
            log.error("All rooms are booked during the requested time");
            throw new AllRoomsBookedException("All rooms are already booked during the requested time.");
        }

        List<ConferenceRoom> availableRooms = rooms.stream()
                .filter(room -> room.getCapacity() >= numberOfPeople)
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .sorted(Comparator.comparingInt(ConferenceRoom::getCapacity))
                .collect(Collectors.toList());

        log.debug("Available rooms found: {}", availableRooms);
        return availableRooms.stream().findFirst();
    }

    private List<ConferenceRoom> findLowerCapacityRooms(LocalTime startTime, LocalTime endTime, int numberOfPeople) {
        log.debug("Finding lower capacity rooms for {} people from {} to {}", numberOfPeople, startTime, endTime);
        List<ConferenceRoom> rooms = conferenceRoomRepository.findAll();

        if (rooms.isEmpty()) {
            log.error("No rooms available in the repository");
            throw new NoRoomAvailableException("No rooms available in the repository.");
        }

        List<ConferenceRoom> lowerCapacityRooms = rooms.stream()
                .filter(room -> room.getCapacity() < numberOfPeople)
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .sorted(Comparator.comparingInt(ConferenceRoom::getCapacity).reversed())
                .collect(Collectors.toList());

        log.debug("Lower capacity rooms found: {}", lowerCapacityRooms);
        return lowerCapacityRooms;
    }

    private boolean isRoomAvailable(ConferenceRoom room, LocalTime startTime, LocalTime endTime) {
        log.debug("Checking availability for room: {} from {} to {}", room.getName(), startTime, endTime);

        List<Booking> existingBookings = bookingRepository.findByRoom(room);
        for (Booking booking : existingBookings) {
            if ((startTime.isBefore(booking.getEndTime()) && !startTime.equals(booking.getEndTime())) &&
                    (endTime.isAfter(booking.getStartTime()) && !endTime.equals(booking.getStartTime()))) {
                log.warn("Room {} is not available due to overlapping booking", room.getName());
                return false;
            }
        }

        for (LocalTime[] maintenanceSlot : room.getMaintenanceSchedule()) {
            if ((startTime.isBefore(maintenanceSlot[1]) && !startTime.equals(maintenanceSlot[1])) &&
                    (endTime.isAfter(maintenanceSlot[0]) && !endTime.equals(maintenanceSlot[0]))) {
                log.warn("Room {} is not available due to maintenance from {} to {}",
                         room.getName(),
                         maintenanceSlot[0],
                         maintenanceSlot[1]);
                throw new MaintenanceTimeException("The requested time overlaps with maintenance windows.");
            }
        }

        log.debug("Room {} is available", room.getName());
        return true;
    }
}