package com.example.interview.service;

import com.example.interview.model.Booking;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.repo.BookingRepository;
import com.example.interview.repo.ConferenceRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<ConferenceRoom> getAvailableRooms(String startTimeStr, String endTimeStr) {
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);

        List<ConferenceRoom> rooms = conferenceRoomRepository.findAll();

        return rooms.stream()
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .collect(Collectors.toList());
    }

    private boolean isRoomAvailable(ConferenceRoom room, LocalTime startTime, LocalTime endTime) {
        // Check maintenance schedule
        for (LocalTime[] maintenanceSlot : room.getMaintenanceSchedule()) {
            if (!endTime.isBefore(maintenanceSlot[0]) && !startTime.isAfter(maintenanceSlot[1])) {
                return false; // Overlaps with maintenance
            }
        }

        // Check existing bookings
        List<Booking> existingBookings = bookingRepository.findByRoomAndTime(room, startTime, endTime);
        return existingBookings.isEmpty(); // Return true if there are no overlapping bookings
    }
}
