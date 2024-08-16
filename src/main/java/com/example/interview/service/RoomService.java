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

        LocalTime startTime = parseAndValidateTime(startTimeStr, "start");
        LocalTime endTime = parseAndValidateTime(endTimeStr, "end");

        if (!startTime.isBefore(endTime)) {
            log.error("Start time {} is not before end time {}", startTime, endTime);
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        return conferenceRoomRepository.findAll().stream()
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .collect(Collectors.toList());
    }

    private LocalTime parseAndValidateTime(String timeStr, String type) {
        try {
            LocalTime time = LocalTime.parse(timeStr);
            log.debug("Parsed {} time: {}", type, time);
            return time;
        } catch (DateTimeParseException e) {
            log.error("Invalid {} time format: {}", type, timeStr, e);
            throw new IllegalArgumentException("Invalid " + type + " time format. Please use HH:mm format (e.g., 14:30).");
        }
    }

    private boolean isRoomAvailable(ConferenceRoom room, LocalTime startTime, LocalTime endTime) {
        log.debug("Checking availability for room: {} between {} and {}", room.getName(), startTime, endTime);

        // Check for overlapping maintenance windows
        boolean maintenanceOverlap = room.getMaintenanceSchedule().stream()
                .anyMatch(slot -> timeOverlaps(startTime, endTime, slot[0], slot[1]));

        if (maintenanceOverlap) {
            log.warn("Room {} is unavailable due to maintenance overlap", room.getName());
            return false;
        }

        // Check for overlapping existing bookings
        boolean bookingOverlap = bookingRepository.findByRoomAndTime(room, startTime, endTime).isEmpty();

        if (!bookingOverlap) {
            log.warn("Room {} is not available due to existing bookings", room.getName());
            return false;
        }

        log.debug("Room {} is available for the requested time slot", room.getName());
        return true;
    }

    private boolean timeOverlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
