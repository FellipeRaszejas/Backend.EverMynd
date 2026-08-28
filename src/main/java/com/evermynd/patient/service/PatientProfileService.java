package com.evermynd.patient.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.evermynd.patient.dto.PatientProfileResponse;
import com.evermynd.patient.dto.UpdatePatientProfileRequest;
import com.evermynd.patient.entity.PatientProfile;
import com.evermynd.patient.exception.PatientProfileNotFoundException;
import com.evermynd.patient.repository.PatientProfileRepository;
import com.evermynd.security.CurrentUserProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final CurrentUserProvider currentUserProvider;

    public PatientProfileResponse getMyProfile() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return PatientProfileResponse.from(getOrThrow(currentUserId));
    }

    public PatientProfileResponse updateMyProfile(UpdatePatientProfileRequest request) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        PatientProfile profile = getOrThrow(currentUserId);

        if (request.birthDate() != null) {
            profile.setBirthDate(request.birthDate());
        }
        if (request.anonymousMode() != null) {
            profile.setAnonymousMode(request.anonymousMode());
        }

        return PatientProfileResponse.from(patientProfileRepository.save(profile));
    }

    private PatientProfile getOrThrow(UUID id) {
        return patientProfileRepository.findById(id)
                .orElseThrow(() -> new PatientProfileNotFoundException("Paciente não encontrado: " + id));
    }
}