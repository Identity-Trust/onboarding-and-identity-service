package com.identityos.onboarding_and_identity_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.identityos.onboarding_and_identity_service.client.AuthenticationClient;
import com.identityos.onboarding_and_identity_service.dto.ApprovalRequest;
import com.identityos.onboarding_and_identity_service.dto.ApplicationRegistrationRequest;
import com.identityos.onboarding_and_identity_service.dto.ApplicationResponse;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityRequest;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityResponse;
import com.identityos.onboarding_and_identity_service.dto.OrganizationAdminSyncResponse;
import com.identityos.onboarding_and_identity_service.dto.OrganizationProfileResponse;
import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationRequest;
import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationResponse;
import com.identityos.onboarding_and_identity_service.repository.OrganizationRepository;
import com.identityos.onboarding_and_identity_service.service.OrganizationRegistrationService;

import java.util.List;

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

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationProfileResponse>> listOrganizations(
            @RequestParam(required = false) String approvalStatus) {
        return ResponseEntity.ok(organizationRepository.findOrganizations(approvalStatus));
    }

    @PostMapping("/organizations/{organizationId}/approval")
    public ResponseEntity<Void> updateOrganizationApproval(
            @PathVariable String organizationId,
            @Valid @RequestBody ApprovalRequest request) {
        organizationRepository.updateApprovalStatus(organizationId, request.decision());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/organizations/{organizationId}/admin-user")
    public ResponseEntity<OrganizationAdminSyncResponse> syncOrganizationAdmin(
            @PathVariable String organizationId) {
        return ResponseEntity.ok(organizationRegistrationService.syncOrganizationAdmin(organizationId));
    }

    @PostMapping("/organizations/{organizationId}/applications")
    public ResponseEntity<ApplicationResponse> registerApplication(
            @PathVariable String organizationId,
            @Valid @RequestBody ApplicationRegistrationRequest request) {
        return ResponseEntity.ok(organizationRepository.insertApplication(organizationId, request));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponse>> listApplications(
            @RequestParam(required = false) String organizationId) {
        return ResponseEntity.ok(organizationRepository.findApplications(organizationId));
    }

    @PostMapping("/applications/{applicationId}/approval")
    public ResponseEntity<Void> updateApplicationApproval(
            @PathVariable String applicationId,
            @Valid @RequestBody ApprovalRequest request) {
        organizationRepository.updateApplicationStatus(applicationId, request.decision());
        return ResponseEntity.noContent().build();
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
