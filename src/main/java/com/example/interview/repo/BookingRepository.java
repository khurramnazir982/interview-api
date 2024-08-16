package com.example.interview.repo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.interview.model.Booking;
import com.example.interview.model.ConferenceRoom;
import org.springframework.stereotype.Repository;

@Repository
public class BookingRepository {

    private final List<Booking> bookings = new ArrayList<>();

    public void save(Booking booking) {
        bookings.add(booking);
    }

    public List<Booking> findByRoom(ConferenceRoom room) {
        return bookings.stream()
                .filter(booking -> booking.getRoom().equals(room))
                .collect(Collectors.toList());
    }

    public List<Booking> findByRoomAndTime(ConferenceRoom room, LocalTime startTime, LocalTime endTime) {
        return bookings.stream()
                .filter(booking -> booking.getRoom().equals(room) &&
                        !endTime.isBefore(booking.getStartTime()) &&
                        !startTime.isAfter(booking.getEndTime()))
                .collect(Collectors.toList());
    }

    public void clear() {
        bookings.clear();
    }
}
