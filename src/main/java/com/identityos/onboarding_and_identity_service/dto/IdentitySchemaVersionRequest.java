package com.identityos.onboarding_and_identity_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record IdentitySchemaVersionRequest(
        @NotBlank String schemaType,
        @NotBlank String schemaName,
        @NotNull Map<String, Object> schemaJson,
        Map<String, Object> configurationJson,
        String changeSummary,
        Boolean submitForApproval
) {
}
