package com.identityos.onboarding_and_identity_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.identityos.onboarding_and_identity_service.client.AuthenticationClient;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityRequest;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityResponse;
import com.identityos.onboarding_and_identity_service.dto.OrganizationAdminSyncResponse;
import com.identityos.onboarding_and_identity_service.dto.OrganizationProfileResponse;
import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationRequest;
import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationResponse;
import com.identityos.onboarding_and_identity_service.repository.OrganizationRepository;
import com.identityos.onboarding_and_identity_service.service.OrganizationRegistrationService;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final AuthenticationClient authenticationClient;
    private final OrganizationRegistrationService organizationRegistrationService;
    private final OrganizationRepository organizationRepository;

    public OnboardingController(
            AuthenticationClient authenticationClient,
            OrganizationRegistrationService organizationRegistrationService,
            OrganizationRepository organizationRepository) {
        this.authenticationClient = authenticationClient;
        this.organizationRegistrationService = organizationRegistrationService;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping("/organizations")
    public ResponseEntity<RegisterOrganizationResponse> registerOrganization(
            @Valid @RequestBody RegisterOrganizationRequest request) {
        return ResponseEntity.ok(organizationRegistrationService.register(request));
    }

    @GetMapping("/organizations/{organizationId}")
    public ResponseEntity<OrganizationProfileResponse> getOrganization(
            @PathVariable String organizationId) {
        return organizationRepository.findByOrganizationId(organizationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/organizations/{organizationId}/admin-user")
    public ResponseEntity<OrganizationAdminSyncResponse> syncOrganizationAdmin(
            @PathVariable String organizationId) {
        return ResponseEntity.ok(organizationRegistrationService.syncOrganizationAdmin(organizationId));
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
