package com.example.interview.exception;

public class MaintenanceTimeException extends RuntimeException {
    public MaintenanceTimeException(String message) {
        super(message);
    }
}