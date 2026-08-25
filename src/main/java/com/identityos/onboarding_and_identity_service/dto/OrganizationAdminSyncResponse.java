package com.identityos.onboarding_and_identity_service.dto;

public record OrganizationAdminSyncResponse(
        String organizationId,
        String keycloakUsername,
        String message,
        boolean actionEmailSent
) {
}
