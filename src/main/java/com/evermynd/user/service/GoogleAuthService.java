package com.evermynd.user.service;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evermynd.security.JwtService;
import com.evermynd.user.dto.AuthResponse;
import com.evermynd.user.dto.GoogleLoginRequest;
import com.evermynd.user.entity.User;
import com.evermynd.user.enums.Role;
import com.evermynd.user.enums.UserStatus;
import com.evermynd.user.exception.InvalidCredentialsException;
import com.evermynd.user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${google.client-id}")
    private String googleClientId;

    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = verifyToken(request.idToken());

        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String fullName = (String) payload.get("name");

        User user = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> createUserFromGoogle(googleId, email, fullName));

        // Se o usuário já existia via email/senha e é o primeiro login por Google, vincula o googleId
        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
            user = userRepository.save(user);
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }

    private User createUserFromGoogle(String googleId, String email, String fullName) {
        User user = User.builder()
                .email(email)
                .googleId(googleId)
                .fullName(fullName != null ? fullName : email)
                .role(Role.PATIENT) // usuário via Google entra como paciente por padrão
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    private GoogleIdToken.Payload verifyToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidCredentialsException("Token do Google inválido");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            throw new InvalidCredentialsException("Falha ao validar token do Google");
        }
    }
}