package com.example.interview.repo;

import java.time.LocalTime;
import java.util.List;

import com.example.interview.config.ConferenceRoomConfig;
import com.example.interview.model.ConferenceRoom;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class ConferenceRoomRepository {

    private final List<ConferenceRoom> rooms;

    @Autowired
    public ConferenceRoomRepository(ConferenceRoomConfig config) {
        this.rooms = config.getRooms().stream()
                .map(this::convertToConferenceRoom)
                .collect(Collectors.toList());
    }

    public List<ConferenceRoom> findAll() {
        return rooms;
    }

    private ConferenceRoom convertToConferenceRoom(ConferenceRoomConfig.ConferenceRoomProperties properties) {
        return ConferenceRoom.builder()
                .name(properties.getName())
                .capacity(properties.getCapacity())
                .maintenanceSchedule(properties.getMaintenanceSchedule().stream()
                                             .map(schedule -> new LocalTime[]{schedule.getStart(), schedule.getEnd()})
                                             .collect(Collectors.toList()))
                .build();
    }
}
