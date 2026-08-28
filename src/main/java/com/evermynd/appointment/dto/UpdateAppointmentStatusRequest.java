package com.evermynd.appointment.dto;

import com.evermynd.appointment.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull(message = "Status é obrigatório")
        AppointmentStatus status
) {
}