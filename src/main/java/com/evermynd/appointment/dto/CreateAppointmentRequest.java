package com.evermynd.appointment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.evermynd.appointment.enums.AppointmentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record CreateAppointmentRequest(
        @NotNull(message = "Médico é obrigatório")
        UUID doctorId,
        @NotNull(message = "Tipo de consulta é obrigatório")
        AppointmentType type,
        @NotNull(message = "Início é obrigatório")
        @Future(message = "Início deve ser uma data futura")
        LocalDateTime startAt,
        @NotNull(message = "Fim é obrigatório")
        @Future(message = "Fim deve ser uma data futura")
        LocalDateTime endAt,
        String notes
) {
}