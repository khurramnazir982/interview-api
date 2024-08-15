package com.example.interview.service;

import static com.example.interview.utils.TestConstants.AMAZE_1100_1200_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.example.interview.model.ConferenceRoom;
import com.example.interview.repo.ConferenceRoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RoomServiceTest {

    @Autowired
    private ConferenceRoomRepository conferenceRoomRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @Test
    public void testGetAvailableRooms_noBookings_noMaintenance_shouldReturnAllRooms() {

        List<ConferenceRoom> availableRooms = roomService.getAvailableRooms("10:00", "11:00");

        assertAvailableRooms(availableRooms, 4);
    }

    @Test
    public void testGetAvailableRooms_allRoomsInMaintenance_shouldReturnEmptyArray() {

        List<ConferenceRoom> availableRooms = roomService.getAvailableRooms("13:00", "14:00");

        assertEquals(0, availableRooms.size());
    }

    @Test
    public void testGetAvailableRooms_allRoomsBooked_shouldReturnEmptyArray() {
        bookingService.bookRoom(AMAZE_1100_1200_REQUEST);
        bookingService.bookRoom(AMAZE_1100_1200_REQUEST);
        bookingService.bookRoom(AMAZE_1100_1200_REQUEST);
        bookingService.bookRoom(AMAZE_1100_1200_REQUEST);

        List<ConferenceRoom> availableRooms = roomService.getAvailableRooms("11:00", "12:30");

        assertEquals(0, availableRooms.size());
    }

    @Test
    public void testGetAvailableRooms_oneTimeslotBooked_shouldReturn3AvailableTimeslots() {

        bookingService.bookRoom(AMAZE_1100_1200_REQUEST);

        List<ConferenceRoom> availableRooms = roomService.getAvailableRooms("11:00", "12:30");

        assertAvailableRooms(availableRooms, 3);
    }

    private void assertAvailableRooms(List<ConferenceRoom> availableRooms, int expectedSize) {
        assertEquals(expectedSize, availableRooms.size());

        List<ConferenceRoom> allRooms = conferenceRoomRepository.findAll();

        for (ConferenceRoom room : availableRooms) {
            ConferenceRoom expectedRoom = allRooms.stream()
                    .filter(r -> r.getName().equals(room.getName()))
                    .findFirst()
                    .orElse(null);

            assert expectedRoom != null;
            assertEquals(expectedRoom.getName(), room.getName());
            assertEquals(expectedRoom.getCapacity(), room.getCapacity());
            assertEquals(expectedRoom.getMaintenanceSchedule(), room.getMaintenanceSchedule());
        }
    }
}