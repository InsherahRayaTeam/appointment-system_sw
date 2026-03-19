package org.example.repository;

import org.example.domain.AppointmentSlot;

import java.util.List;

public interface AppointmentRepository {

    List<AppointmentSlot> findAll();

    List<AppointmentSlot> findAvailable();
}

