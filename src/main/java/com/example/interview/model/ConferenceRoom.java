package com.example.interview.model;

import java.time.LocalTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConferenceRoom {

    private String name;
    private int capacity;
    private List<LocalTime[]> maintenanceSchedule;

    public ConferenceRoom(final String name, final int capacity, final List<LocalTime[]> maintenanceSchedule) {
        this.name = name;
        this.capacity = capacity;
        this.maintenanceSchedule = maintenanceSchedule;
    }
}