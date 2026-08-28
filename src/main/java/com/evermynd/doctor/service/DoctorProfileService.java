package com.evermynd.doctor.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.evermynd.doctor.dto.DoctorProfileResponse;
import com.evermynd.doctor.dto.UpdateDoctorProfileRequest;
import com.evermynd.doctor.entity.DoctorProfile;
import com.evermynd.doctor.enums.VerificationStatus;
import com.evermynd.doctor.exception.DoctorProfileNotFoundException;
import com.evermynd.doctor.repository.DoctorProfileRepository;
import com.evermynd.security.CurrentUserProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final CurrentUserProvider currentUserProvider;

    // Busca pública — só médicos aprovados aparecem
    public List<DoctorProfileResponse> listApproved() {
        return doctorProfileRepository.findByVerificationStatus(VerificationStatus.APPROVED).stream()
                .map(DoctorProfileResponse::from)
                .toList();
    }

    public DoctorProfileResponse findById(UUID id) {
        return DoctorProfileResponse.from(getOrThrow(id));
    }

    public DoctorProfileResponse updateMyProfile(UpdateDoctorProfileRequest request) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        DoctorProfile profile = getOrThrow(currentUserId);

        profile.setSpecialty(request.specialty());
        profile.setBio(request.bio());
        profile.setPriceRange(request.priceRange());

        return DoctorProfileResponse.from(doctorProfileRepository.save(profile));
    }

    // --- usados só pelo AdminController ---

    public List<DoctorProfileResponse> listPending() {
        return doctorProfileRepository.findByVerificationStatus(VerificationStatus.PENDING).stream()
                .map(DoctorProfileResponse::from)
                .toList();
    }

    public DoctorProfileResponse setVerificationStatus(UUID doctorId, VerificationStatus status) {
        DoctorProfile profile = getOrThrow(doctorId);
        profile.setVerificationStatus(status);
        return DoctorProfileResponse.from(doctorProfileRepository.save(profile));
    }

    private DoctorProfile getOrThrow(UUID id) {
        return doctorProfileRepository.findById(id)
                .orElseThrow(() -> new DoctorProfileNotFoundException("Médico não encontrado: " + id));
    }
}