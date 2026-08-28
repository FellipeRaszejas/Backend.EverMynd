package com.evermynd.appointment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.evermynd.appointment.entity.Appointment;
import com.evermynd.appointment.enums.AppointmentStatus;
import com.evermynd.appointment.enums.AppointmentType;

public record AppointmentResponse(
        UUID id,
        UUID doctorId,
        String doctorName,
        UUID patientId,
        String patientName,
        AppointmentType type,
        AppointmentStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String meetingUrl,
        String notes
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getUser().getFullName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getUser().getFullName(),
                appointment.getType(),
                appointment.getStatus(),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getMeetingUrl(),
                appointment.getNotes()
        );
    }
}