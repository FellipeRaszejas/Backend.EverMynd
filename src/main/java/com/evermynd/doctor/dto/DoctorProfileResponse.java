package com.evermynd.doctor.dto;

import java.util.UUID;

import com.evermynd.doctor.entity.DoctorProfile;
import com.evermynd.doctor.enums.VerificationStatus;

public record DoctorProfileResponse(
        UUID id,
        String fullName,
        String email,
        String specialty,
        String bio,
        String priceRange,
        VerificationStatus verificationStatus
) {
    public static DoctorProfileResponse from(DoctorProfile profile) {
        return new DoctorProfileResponse(
                profile.getId(),
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                profile.getSpecialty(),
                profile.getBio(),
                profile.getPriceRange(),
                profile.getVerificationStatus()
        );
    }
}