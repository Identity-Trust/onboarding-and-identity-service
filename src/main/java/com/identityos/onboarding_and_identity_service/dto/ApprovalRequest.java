package com.identityos.onboarding_and_identity_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ApprovalRequest(
        @NotBlank String decision
) {
}
