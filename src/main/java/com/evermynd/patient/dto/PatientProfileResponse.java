package com.evermynd.patient.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.evermynd.patient.entity.PatientProfile;

public record PatientProfileResponse(
        UUID id,
        String fullName,
        LocalDate birthDate,
        boolean anonymousMode
) {
    public static PatientProfileResponse from(PatientProfile profile) {
        return new PatientProfileResponse(
                profile.getId(),
                profile.getUser().getFullName(),
                profile.getBirthDate(),
                profile.isAnonymousMode()
        );
    }
}