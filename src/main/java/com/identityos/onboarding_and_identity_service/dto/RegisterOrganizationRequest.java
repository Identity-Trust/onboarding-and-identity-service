package com.identityos.onboarding_and_identity_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record RegisterOrganizationRequest(
        UUID entityId,
        String organizationId,
        @NotBlank String organizationName,
        @NotBlank String organizationType,
        @NotBlank String countryCode,
        @Email String officialEmail,
        String officialPhone,
        String registrationNumber,
        String registrationAuthority,
        LocalDate incorporationDate,
        String verificationIdType,
        String verificationId,
        String verificationIdVerifyStatus,
        String websiteUrl,
        String logoUrl,
        @NotBlank String representativeName,
        @NotBlank @Email String representativeEmail,
        String representativeMobile,
        String representativeDesignation,
        String representativeEmployeeId,
        String addressType,
        String addressLine1,
        String addressLine2,
        String city,
        String district,
        String state,
        String postalCode,
        String addressProofRef,
        UUID approvedBy
) {
}
