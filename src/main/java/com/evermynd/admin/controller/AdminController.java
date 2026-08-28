package com.evermynd.admin.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evermynd.doctor.dto.DoctorProfileResponse;
import com.evermynd.doctor.dto.VerifyDoctorRequest;
import com.evermynd.doctor.service.DoctorProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DoctorProfileService doctorProfileService;

    @GetMapping("/pending")
    public ResponseEntity<List<DoctorProfileResponse>> listPending() {
        return ResponseEntity.ok(doctorProfileService.listPending());
    }

    @PatchMapping("/{id}/verification")
    public ResponseEntity<DoctorProfileResponse> verify(
            @PathVariable UUID id, @Valid @RequestBody VerifyDoctorRequest request) {
        return ResponseEntity.ok(doctorProfileService.setVerificationStatus(id, request.verificationStatus()));
    }
}