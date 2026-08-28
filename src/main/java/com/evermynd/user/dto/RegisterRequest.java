package com.evermynd.user.dto;

import com.evermynd.user.enums.RegisterRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Nome é obrigatório")
        String fullName,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String password,

        String phone,

        @NotNull(message = "Tipo de cadastro é obrigatório")
        RegisterRole role,

        // preenchidos só quando role == DOCTOR
        String specialty,
        String bio,
        String priceRange,

        // preenchido só quando role == PATIENT
        String birthDate // ISO "yyyy-MM-dd", convertido no service
) {
}