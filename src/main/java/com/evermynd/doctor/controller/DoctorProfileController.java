package com.evermynd.doctor.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evermynd.doctor.dto.DoctorProfileResponse;
import com.evermynd.doctor.dto.UpdateDoctorProfileRequest;
import com.evermynd.doctor.service.DoctorProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorProfileController {

    private final DoctorProfileService doctorProfileService;

    @GetMapping
    public ResponseEntity<List<DoctorProfileResponse>> listAll() {
        return ResponseEntity.ok(doctorProfileService.listApproved());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorProfileResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorProfileService.findById(id));
    }

    @PutMapping("/me")
    public ResponseEntity<DoctorProfileResponse> updateMine(@Valid @RequestBody UpdateDoctorProfileRequest request) {
        return ResponseEntity.ok(doctorProfileService.updateMyProfile(request));
    }
}