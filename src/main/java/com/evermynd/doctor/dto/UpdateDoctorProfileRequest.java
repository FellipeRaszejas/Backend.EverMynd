package com.evermynd.doctor.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateDoctorProfileRequest(
        @NotBlank(message = "Especialidade é obrigatória")
        String specialty,
        String bio,
        String priceRange
) {
}