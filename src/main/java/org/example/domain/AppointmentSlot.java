package org.example.domain;

public class AppointmentSlot {

    private String time;
    private boolean booked;

    public AppointmentSlot(String time) {
        this.time = time;
        this.booked = false;
    }

    public String getTime() {
        return time;
    }

    public boolean isBooked() {
        return booked;
    }

    public void book() {
        booked = true;
    }
}