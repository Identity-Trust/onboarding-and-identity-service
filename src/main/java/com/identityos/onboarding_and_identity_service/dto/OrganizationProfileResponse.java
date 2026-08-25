package com.identityos.onboarding_and_identity_service.dto;

public record OrganizationProfileResponse(
        String organizationId,
        String organizationName,
        String organizationType,
        String countryCode,
        String officialEmail,
        String officialPhone,
        String registrationNumber,
        String registrationAuthority,
        String verificationIdType,
        String verificationId,
        String verificationIdVerifyStatus,
        String websiteUrl,
        String logoUrl,
        String status,
        String approvalStatus
) {
}
