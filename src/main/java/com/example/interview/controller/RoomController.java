package com.example.interview.controller;

import java.util.List;

import com.example.interview.model.ConferenceRoom;
import com.example.interview.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/available")
    public ResponseEntity<List<ConferenceRoom>> getAvailableRooms(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<ConferenceRoom> availableRooms = roomService.getAvailableRooms(startTime, endTime);
        return ResponseEntity.ok(availableRooms);
    }
}
