package com.identityos.onboarding_and_identity_service.dto;

import java.time.LocalDateTime;

public record IdentitySchemaVersionResponse(
        String schemaId,
        String versionId,
        String organizationId,
        String organizationName,
        String applicationId,
        String applicationName,
        String schemaType,
        String schemaName,
        Integer versionNumber,
        String schemaJson,
        String configurationJson,
        String status,
        String changeSummary,
        LocalDateTime createdAt,
        LocalDateTime publishedAt
) {
}
