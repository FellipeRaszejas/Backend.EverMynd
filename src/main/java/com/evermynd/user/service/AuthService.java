package com.evermynd.user.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.evermynd.doctor.entity.DoctorProfile;
import com.evermynd.doctor.repository.DoctorProfileRepository;
import com.evermynd.patient.entity.PatientProfile;
import com.evermynd.patient.repository.PatientProfileRepository;
import com.evermynd.security.JwtService;
import com.evermynd.user.dto.AuthResponse;
import com.evermynd.user.dto.LoginRequest;
import com.evermynd.user.dto.RegisterRequest;
import com.evermynd.user.entity.User;
import com.evermynd.user.enums.Role;
import com.evermynd.user.enums.UserStatus;
import com.evermynd.user.exception.EmailAlreadyInUseException;
import com.evermynd.user.exception.InvalidCredentialsException;
import com.evermynd.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException("Este email já está cadastrado");
        }

        // RegisterRole só tem PATIENT/DOCTOR — não existe caminho pra virar ADMIN aqui
        Role role = Role.valueOf(request.role().name());

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        switch (role) {
            case PATIENT -> createPatientProfile(user, request);
            case DOCTOR -> createDoctorProfile(user, request);
            default -> throw new IllegalStateException("Role inesperada no cadastro público: " + role);
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Email ou senha inválidos"));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Email ou senha inválidos");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }

    private void createPatientProfile(User user, RegisterRequest request) {
        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .birthDate(request.birthDate() != null ? LocalDate.parse(request.birthDate()) : null)
                .anonymousMode(false)
                .build();

        patientProfileRepository.save(profile);
    }

    private void createDoctorProfile(User user, RegisterRequest request) {
        DoctorProfile profile = DoctorProfile.builder()
                .user(user)
                .specialty(request.specialty())
                .bio(request.bio())
                .priceRange(request.priceRange())
                .build();

        doctorProfileRepository.save(profile);
    }
}