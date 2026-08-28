package com.identityos.onboarding_and_identity_service.dto;

public record HostedIdentityAuthResponse(
        boolean success,
        String message,
        String username,
        String keycloakUserId,
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
