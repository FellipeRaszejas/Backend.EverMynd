package com.evermynd.doctor.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evermynd.doctor.entity.DoctorProfile;
import com.evermynd.doctor.enums.VerificationStatus;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
    List<DoctorProfile> findByVerificationStatus(VerificationStatus status);
}