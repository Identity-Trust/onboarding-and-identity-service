package com.identityos.onboarding_and_identity_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record RegisterOrganizationRequest(
        UUID entityId,
        String organizationId,
        @NotBlank String organizationName,
        @NotBlank String organizationType,
        @NotBlank String countryCode,
        @NotBlank @Email String officialEmail,
        @NotBlank @Pattern(regexp = "^\\+?[0-9][0-9\\s-]{6,18}$", message = "Official phone number must contain 7 to 15 digits and may include spaces, hyphens, or a leading plus.")
        String officialPhone,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9/-]{3,30}$", message = "Registration number must be 3 to 30 characters and use only letters, numbers, slash, or hyphen.")
        String registrationNumber,
        String registrationAuthority,
        LocalDate incorporationDate,
        String verificationIdType,
        String verificationId,
        String verificationIdVerifyStatus,
        @Pattern(regexp = "^$|^(https?://)?([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}(/.*)?$", message = "Website must be a valid URL or domain.")
        String websiteUrl,
        String logoUrl,
        @NotBlank String representativeName,
        @NotBlank @Email String representativeEmail,
        @Pattern(regexp = "^$|^\\+?[0-9][0-9\\s-]{6,18}$", message = "Representative mobile number must contain 7 to 15 digits and may include spaces, hyphens, or a leading plus.")
        String representativeMobile,
        @Pattern(regexp = "^$|^[A-Za-z ]{2,60}$", message = "Designation must contain only letters and spaces.")
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
