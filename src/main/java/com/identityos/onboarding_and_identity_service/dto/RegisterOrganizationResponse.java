package com.identityos.onboarding_and_identity_service.dto;

public record RegisterOrganizationResponse(
        String organizationId,
        String keycloakUsername,
        String message,
        boolean verificationEmailSent
) {
}
