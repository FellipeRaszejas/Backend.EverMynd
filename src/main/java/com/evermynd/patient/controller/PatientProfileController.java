package com.evermynd.patient.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evermynd.patient.dto.PatientProfileResponse;
import com.evermynd.patient.dto.UpdatePatientProfileRequest;
import com.evermynd.patient.service.PatientProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    @GetMapping("/me")
    public ResponseEntity<PatientProfileResponse> getMine() {
        return ResponseEntity.ok(patientProfileService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<PatientProfileResponse> updateMine(@Valid @RequestBody UpdatePatientProfileRequest request) {
        return ResponseEntity.ok(patientProfileService.updateMyProfile(request));
    }
}