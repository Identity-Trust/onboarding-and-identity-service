package com.identityos.onboarding_and_identity_service.dto;

public class CreateIdentityRequest {

    private String organizationId;
    private String applicationId;

    public CreateIdentityRequest() {
    }

    public CreateIdentityRequest(
            String organizationId,
            String applicationId) {

        this.organizationId = organizationId;
        this.applicationId = applicationId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
}
