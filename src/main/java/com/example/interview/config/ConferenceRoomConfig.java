package com.example.interview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "conference-rooms")
public class ConferenceRoomConfig {

    private List<ConferenceRoomProperties> rooms;

    @Data
    public static class ConferenceRoomProperties {
        private String name;
        private int capacity;
        private List<MaintenanceSchedule> maintenanceSchedule;
    }

    @Data
    public static class MaintenanceSchedule {
        private LocalTime start;
        private LocalTime end;
    }
}
