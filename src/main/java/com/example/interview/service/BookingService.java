package com.example.interview.service;

import com.example.interview.dto.BookingRequest;
import com.example.interview.model.Booking;
import com.example.interview.model.ConferenceRoom;
import com.example.interview.repo.BookingRepository;
import com.example.interview.repo.ConferenceRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public String bookRoom(BookingRequest request) throws Exception {
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = LocalTime.parse(request.getEndTime());
        int numberOfPeople = request.getNumberOfPeople();

        // Validate booking request
        if (numberOfPeople <= 1) {
            throw new Exception("Number of people should be greater than 1.");
        }

        // Find the best available room
        Optional<ConferenceRoom> availableRoom = findAvailableRoom(startTime, endTime, numberOfPeople);

        if (availableRoom.isPresent()) {
            ConferenceRoom room = availableRoom.get();

            // Creating a Booking using Lombok's @Builder
            Booking booking = Booking.builder()
                    .room(room)
                    .startTime(startTime)
                    .endTime(endTime)
                    .numberOfPeople(numberOfPeople)
                    .build();

            bookingRepository.save(booking);

            return String.format("Room '%s' booked successfully for %d people from %s to %s.",
                                 room.getName(), numberOfPeople, startTime, endTime);
        } else {
            throw new Exception("No suitable room available for the requested time.");
        }
    }

    private Optional<ConferenceRoom> findAvailableRoom(LocalTime startTime, LocalTime endTime, int numberOfPeople) {
        List<ConferenceRoom> rooms = conferenceRoomRepository.findAll();

        // Filter rooms based on capacity and availability
        return rooms.stream()
                .filter(room -> room.getCapacity() >= numberOfPeople)
                .filter(room -> isRoomAvailable(room, startTime, endTime))
                .min(Comparator.comparingInt(ConferenceRoom::getCapacity)); // Find the room with the smallest capacity that fits the request
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
