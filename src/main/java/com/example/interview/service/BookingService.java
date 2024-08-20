package com.example.interview.service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.example.interview.dto.BookingRequest;
import com.example.interview.exception.*;
import com.example.interview.model.Booking;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.repo.BookingRepository;
import com.example.interview.repo.ConferenceRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingService {

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private final AtomicLong idGenerator = new AtomicLong();

    public String bookRoom(BookingRequest request) {
        log.info("bookRoom called with request: {}", request);

        LocalTime startTime = parseTime(request.getStartTime(), "start");
        LocalTime endTime = parseTime(request.getEndTime(), "end");
        int numberOfPeople = request.getNumberOfPeople();

        validateBookingRequest(startTime, endTime, numberOfPeople);

        // Find a suitable room or throw the appropriate exception
        ConferenceRoom room = findAvailableRoom(startTime, endTime, numberOfPeople)
                .orElseThrow(() -> findLowerCapacityRooms(startTime, endTime, numberOfPeople).isEmpty()
                        ? new AllRoomsBookedException("All rooms are already booked during the requested time.")
                        : handleNoRoomAvailable(startTime, endTime, numberOfPeople));

        return bookRoom(room, startTime, endTime, numberOfPeople);
    }

    private LocalTime parseTime(String time, String type) {
        try {
            return LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            log.error("Invalid {} time format: {}", type, time, e);
            throw new IllegalArgumentException("Invalid " + type + " time format. Please use HH:mm format (e.g., 14:30).");
        }
    }

    private void validateBookingRequest(LocalTime startTime, LocalTime endTime, int numberOfPeople) {
        if (numberOfPeople <= 1) {
            log.error("Invalid number of people: {}", numberOfPeople);
            throw new InvalidNumberOfPeopleException("Number of people should be greater than 1.");
        }
        validateTimeInterval(startTime, endTime);
        validateBookingDuration(startTime, endTime);
    }

    private void validateTimeInterval(LocalTime startTime, LocalTime endTime) {
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
        Duration duration = Duration.between(startTime, endTime);
        long minutes = duration.toMinutes();

        if (minutes < 30) {
            log.error("Invalid booking duration: {} minutes", minutes);
            throw new InvalidTimeIntervalException("Booking duration must be at least 30 minutes.");
        }
        if (minutes > 300) {
            log.error("Booking duration exceeds 5 hours: {} minutes", minutes);
            throw new InvalidTimeIntervalException("Booking duration cannot exceed 5 hours.");
        }
    }

    private Optional<ConferenceRoom> findAvailableRoom(LocalTime startTime, LocalTime endTime, int numberOfPeople) {
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

        return rooms.stream()
                .filter(room -> room.getCapacity() >= numberOfPeople)
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .sorted(Comparator.comparingInt(ConferenceRoom::getCapacity))
                .findFirst();
    }

    private RuntimeException handleNoRoomAvailable(LocalTime startTime, LocalTime endTime, int numberOfPeople) {
        List<ConferenceRoom> lowerCapacityRooms = findLowerCapacityRooms(startTime, endTime, numberOfPeople);

        StringBuilder message = new StringBuilder("All rooms suitable for ")
                .append(numberOfPeople)
                .append(" people are booked, but the following rooms with lower capacity are available during the requested time:\n");

        lowerCapacityRooms.forEach(room -> message.append(
                String.format("Room '%s' with a capacity of %d people.\n", room.getName(), room.getCapacity())));

        log.warn("Rooms with lower capacity found: {}", lowerCapacityRooms);
        return new NoRoomAvailableException(message.toString().trim());
    }

    private String bookRoom(ConferenceRoom room, LocalTime startTime, LocalTime endTime, int numberOfPeople) {
        log.info("Booking room: {} for {} people from {} to {}", room.getName(), numberOfPeople, startTime, endTime);

        Booking booking = Booking.builder()
                .id(idGenerator.incrementAndGet())
                .room(room)
                .startTime(startTime)
                .endTime(endTime)
                .numberOfPeople(numberOfPeople)
                .build();

        bookingRepository.save(booking);
        log.info("Room '{}' booked successfully", room.getName());

        return String.format("Room '%s' booked successfully for %d people from %s to %s.",
                             room.getName(), numberOfPeople, startTime, endTime);
    }

    private List<ConferenceRoom> findLowerCapacityRooms(LocalTime startTime, LocalTime endTime, int numberOfPeople) {
        return conferenceRoomRepository.findAll().stream()
                .filter(room -> room.getCapacity() < numberOfPeople)
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .sorted(Comparator.comparingInt(ConferenceRoom::getCapacity).reversed())
                .collect(Collectors.toList());
    }

    public void deleteBooking(Long bookingId) {
        log.info("Attempting to delete booking with id: {}", bookingId);
        if (bookingRepository.findById(bookingId).isEmpty()) {
            throw new BookingNotFoundException("Booking with ID " + bookingId + " not found.");
        }

        bookingRepository.deleteById(bookingId);
        log.info("Booking with id {} deleted successfully", bookingId);
    }

    private boolean isRoomAvailable(ConferenceRoom room, LocalTime startTime, LocalTime endTime) {
        boolean isAvailable = bookingRepository.findByRoom(room).stream()
                .noneMatch(booking -> timeOverlaps(startTime, endTime, booking.getStartTime(), booking.getEndTime()));

        if (!isAvailable) {
            return false;
        }

        // Collect all overlapping maintenance windows
        List<LocalTime[]> overlappingMaintenance = room.getMaintenanceSchedule().stream()
                .filter(slot -> timeOverlaps(startTime, endTime, slot[0], slot[1]))
                .collect(Collectors.toList());

        if (!overlappingMaintenance.isEmpty()) {
            String maintenanceMessage = buildMaintenanceExceptionMessage(room, overlappingMaintenance);
            log.warn(maintenanceMessage);
            throw new MaintenanceTimeException(maintenanceMessage);
        }

        return true;
    }

    private String buildMaintenanceExceptionMessage(ConferenceRoom room, List<LocalTime[]> overlappingMaintenance) {
        StringBuilder message = new StringBuilder("The requested time overlaps with the following maintenance windows for room: ")
                .append(room.getName())
                .append(": ");

        overlappingMaintenance.forEach(slot -> message.append(String.format("[%s to %s] ", slot[0], slot[1]))
        );

        return message.toString().trim();
    }

    private boolean timeOverlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    public Booking getBookingById(final Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    String message = "Booking with ID " + bookingId + " not found.";
                    return new BookingNotFoundException(message);
                });
    }
}
