package com.example.interview.service;

import com.example.interview.exception.MaintenanceTimeException;
import com.example.interview.model.Booking;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.repo.BookingRepository;
import com.example.interview.repo.ConferenceRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoomService {

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<ConferenceRoom> getAvailableRooms(String startTimeStr, String endTimeStr) {
        log.info("getAvailableRooms called with startTime: {} and endTime: {}", startTimeStr, endTimeStr);

        LocalTime startTime;
        LocalTime endTime;

        // Validate and parse time strings
        try {
            startTime = LocalTime.parse(startTimeStr);
            endTime = LocalTime.parse(endTimeStr);
            log.debug("Parsed startTime: {}, endTime: {}", startTime, endTime);
        } catch (DateTimeParseException e) {
            log.error("Invalid time format for startTime: {} or endTime: {}", startTimeStr, endTimeStr, e);
            throw new IllegalArgumentException("Invalid time format. Please use HH:mm format (e.g., 14:30).");
        }

        // Ensure that startTime is before endTime
        if (!startTime.isBefore(endTime)) {
            log.error("Start time {} is not before end time {}", startTime, endTime);
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        log.info("Fetching all conference rooms from repository");
        List<ConferenceRoom> rooms = conferenceRoomRepository.findAll();

        log.debug("Filtering available rooms for the given time slot");
        List<ConferenceRoom> availableRooms = rooms.stream()
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .collect(Collectors.toList());

        log.info("Found {} available rooms", availableRooms.size());
        return availableRooms;
    }

    private boolean isRoomAvailable(ConferenceRoom room, LocalTime startTime, LocalTime endTime) {
        log.debug("Checking availability for room: {} between {} and {}", room.getName(), startTime, endTime);

        // Check maintenance schedule
        for (LocalTime[] maintenanceSlot : room.getMaintenanceSchedule()) {
            if ((startTime.isBefore(maintenanceSlot[1]) && !startTime.equals(maintenanceSlot[1])) &&
                    (endTime.isAfter(maintenanceSlot[0]) && !endTime.equals(maintenanceSlot[0]))) {
                log.warn("Room {} is unavailable due to maintenance between {} and {}", room.getName(), maintenanceSlot[0], maintenanceSlot[1]);
                return false; // Overlaps with maintenance
            }
        }

        // Check existing bookings
        log.debug("Checking existing bookings for room: {}", room.getName());
        List<Booking> existingBookings = bookingRepository.findByRoomAndTime(room, startTime, endTime);
        boolean isAvailable = existingBookings.isEmpty();

        if (isAvailable) {
            log.debug("Room {} is available for the requested time slot", room.getName());
        } else {
            log.warn("Room {} is not available due to existing bookings", room.getName());
        }

        return isAvailable;
    }
}