package com.evermynd.patient.dto;

import java.time.LocalDate;

public record UpdatePatientProfileRequest(
        LocalDate birthDate,
        Boolean anonymousMode
) {
}