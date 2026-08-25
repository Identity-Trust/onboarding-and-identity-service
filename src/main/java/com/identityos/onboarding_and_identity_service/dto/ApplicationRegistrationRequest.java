package com.identityos.onboarding_and_identity_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplicationRegistrationRequest(
        @NotBlank String applicationName,
        @NotBlank String applicationType,
        String description,
        @NotBlank String redirectUri
) {
}
