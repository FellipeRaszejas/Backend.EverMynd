package com.evermynd.doctor.dto;

import com.evermynd.doctor.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;

public record VerifyDoctorRequest(
        @NotNull(message = "Status é obrigatório")
        VerificationStatus verificationStatus
) {
}