package com.evermynd.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evermynd.appointment.entity.Appointment;
import com.evermynd.appointment.enums.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPatientId(UUID patientId);

    List<Appointment> findByDoctorId(UUID doctorId);

    List<Appointment> findByDoctorIdAndStartAtBetween(
            UUID doctorId, LocalDateTime rangeStart, LocalDateTime rangeEnd);

    boolean existsByDoctorIdAndStatusNotAndStartAtLessThanAndEndAtGreaterThan(
            UUID doctorId, AppointmentStatus excludedStatus,
            LocalDateTime requestedEndAt, LocalDateTime requestedStartAt);
}