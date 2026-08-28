package com.evermynd.appointment.service;


import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.evermynd.appointment.dto.AppointmentResponse;
import com.evermynd.appointment.dto.CreateAppointmentRequest;
import com.evermynd.appointment.entity.Appointment;
import com.evermynd.appointment.enums.AppointmentStatus;
import com.evermynd.appointment.exception.AppointmentConflictException;
import com.evermynd.appointment.exception.AppointmentNotFoundException;
import com.evermynd.appointment.repository.AppointmentRepository;
import com.evermynd.doctor.entity.DoctorProfile;
import com.evermynd.doctor.exception.DoctorProfileNotFoundException;
import com.evermynd.doctor.repository.DoctorProfileRepository;
import com.evermynd.patient.entity.PatientProfile;
import com.evermynd.patient.exception.PatientProfileNotFoundException;
import com.evermynd.patient.repository.PatientProfileRepository;
import com.evermynd.security.CurrentUserProvider;
import com.evermynd.appointment.exception.AppointmentAccessDeniedException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final CurrentUserProvider currentUserProvider;

    public AppointmentResponse create(CreateAppointmentRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new IllegalArgumentException("Horário de término deve ser após o horário de início");
        }

        UUID patientId = currentUserProvider.getCurrentUserId();

        PatientProfile patient = patientProfileRepository.findById(patientId)
                .orElseThrow(() -> new PatientProfileNotFoundException("Perfil de paciente não encontrado"));

        DoctorProfile doctor = doctorProfileRepository.findById(request.doctorId())
                .orElseThrow(() -> new DoctorProfileNotFoundException(
                        "Médico não encontrado: " + request.doctorId()));

        boolean hasConflict = appointmentRepository
                .existsByDoctorIdAndStatusNotAndStartAtLessThanAndEndAtGreaterThan(
                        doctor.getId(), AppointmentStatus.CANCELLED, request.endAt(), request.startAt());

        if (hasConflict) {
            throw new AppointmentConflictException("Médico já possui consulta nesse horário");
        }

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .type(request.type())
                .status(AppointmentStatus.PENDING)
                .startAt(request.startAt())
                .endAt(request.endAt())
                .notes(request.notes())
                .build();

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> findByPatient(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    public List<AppointmentResponse> findByDoctor(UUID doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    public AppointmentResponse updateStatus(UUID appointmentId, AppointmentStatus newStatus) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Consulta não encontrada: " + appointmentId));

        boolean isDoctor = appointment.getDoctor().getId().equals(currentUserId);
        boolean isPatient = appointment.getPatient().getId().equals(currentUserId);

        if (!isDoctor && !isPatient) {
            throw new AppointmentAccessDeniedException("Usuário não tem permissão para alterar essa consulta");
        }

        appointment.setStatus(newStatus);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }
}