package com.identityos.onboarding_and_identity_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.identityos.onboarding_and_identity_service.client.AuthenticationClient;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityRequest;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityResponse;
import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationRequest;
import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationResponse;
import com.identityos.onboarding_and_identity_service.service.OrganizationRegistrationService;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final AuthenticationClient authenticationClient;
    private final OrganizationRegistrationService organizationRegistrationService;

    public OnboardingController(
            AuthenticationClient authenticationClient,
            OrganizationRegistrationService organizationRegistrationService) {
        this.authenticationClient = authenticationClient;
        this.organizationRegistrationService = organizationRegistrationService;
    }

    @PostMapping("/organizations")
    public ResponseEntity<RegisterOrganizationResponse> registerOrganization(
            @Valid @RequestBody RegisterOrganizationRequest request) {
        return ResponseEntity.ok(organizationRegistrationService.register(request));
    }

    @PostMapping("/test-authentication")
    public ResponseEntity<CreateIdentityResponse> testAuthentication(
        @RequestBody CreateIdentityRequest request,
        @RequestHeader("Authorization") String authorization) {

    CreateIdentityResponse response =
            authenticationClient.createIdentity(request, authorization);

    return ResponseEntity.ok(response);
}
}
