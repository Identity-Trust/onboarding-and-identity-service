package com.identityos.onboarding_and_identity_service.service;

import com.identityos.onboarding_and_identity_service.client.KeycloakAdminClient;
import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationRequest;
import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationResponse;
import com.identityos.onboarding_and_identity_service.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizationRegistrationService {
    private final OrganizationRepository organizationRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    public OrganizationRegistrationService(
            OrganizationRepository organizationRepository,
            KeycloakAdminClient keycloakAdminClient) {
        this.organizationRepository = organizationRepository;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Transactional
    public RegisterOrganizationResponse register(RegisterOrganizationRequest request) {
        String organizationId = request.organizationId() == null || request.organizationId().isBlank()
            ? "org_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
            : request.organizationId();

        organizationRepository.insert(request, organizationId);
        boolean verificationEmailSent = keycloakAdminClient.createOrganizationAdmin(
            organizationId,
                request.representativeName(),
                request.representativeEmail());

        return new RegisterOrganizationResponse(
            organizationId,
            organizationId,
                "Organization registered. Check the representative email to verify the organization admin account.",
                verificationEmailSent);
    }
}
