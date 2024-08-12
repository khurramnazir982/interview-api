package com.example.interview.repo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.interview.model.ConferenceRoom;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

@Repository
public class ConferenceRoomRepository {

    private final List<ConferenceRoom> rooms = List.of(
            ConferenceRoom.builder().name("Amaze").capacity(3).maintenanceSchedule(getDefaultMaintenanceSchedule()).build(),
            ConferenceRoom.builder().name("Beauty").capacity(7).maintenanceSchedule(getDefaultMaintenanceSchedule()).build(),
            ConferenceRoom.builder().name("Inspire").capacity(12).maintenanceSchedule(getDefaultMaintenanceSchedule()).build(),
            ConferenceRoom.builder().name("Strive").capacity(20).maintenanceSchedule(getDefaultMaintenanceSchedule()).build()
    );

    public List<ConferenceRoom> findAll() {
        return rooms;
    }

    private List<LocalTime[]> getDefaultMaintenanceSchedule() {
        return List.of(
                new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(9, 15)},
                new LocalTime[]{LocalTime.of(13, 0), LocalTime.of(13, 15)},
                new LocalTime[]{LocalTime.of(17, 0), LocalTime.of(17, 15)}
        );
    }
}
