package com.evermynd.user.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "ID token do Google é obrigatório")
        String idToken
) {
}