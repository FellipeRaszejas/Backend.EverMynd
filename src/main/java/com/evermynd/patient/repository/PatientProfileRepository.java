package com.evermynd.patient.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evermynd.patient.entity.PatientProfile;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, UUID> {
}