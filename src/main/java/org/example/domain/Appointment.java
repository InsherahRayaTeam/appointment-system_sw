package org.example.domain;

import java.time.LocalDateTime;

public class Appointment {
    private String id;
    private LocalDateTime startTime;
    private int durationMinutes;
    private int participants;

    public Appointment(String id, LocalDateTime startTime, int durationMinutes, int participants) {
        this.id = id;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.participants = participants;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getParticipants() {
        return participants;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}