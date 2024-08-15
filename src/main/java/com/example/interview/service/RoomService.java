package com.example.interview.service;

import com.example.interview.exception.MaintenanceTimeException;
import com.example.interview.model.Booking;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.repo.BookingRepository;
import com.example.interview.repo.ConferenceRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<ConferenceRoom> getAvailableRooms(String startTimeStr, String endTimeStr) {
        LocalTime startTime;
        LocalTime endTime;

        // Validate and parse time strings
        try {
            startTime = LocalTime.parse(startTimeStr);
            endTime = LocalTime.parse(endTimeStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Please use HH:mm format (e.g., 14:30).");
        }

        // Ensure that startTime is before endTime
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        List<ConferenceRoom> rooms = conferenceRoomRepository.findAll();

        return rooms.stream()
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .collect(Collectors.toList());
    }


    private boolean isRoomAvailable(ConferenceRoom room, LocalTime startTime, LocalTime endTime) {
        // Check maintenance schedule
        for (LocalTime[] maintenanceSlot : room.getMaintenanceSchedule()) {
            if ((startTime.isBefore(maintenanceSlot[1]) && !startTime.equals(maintenanceSlot[1])) &&
                    (endTime.isAfter(maintenanceSlot[0]) && !endTime.equals(maintenanceSlot[0]))) {
                return false; // Overlaps with maintenance
            }
        }

        // Check existing bookings
        List<Booking> existingBookings = bookingRepository.findByRoomAndTime(room, startTime, endTime);
        return existingBookings.isEmpty(); // Return true if there are no overlapping bookings
    }
}
