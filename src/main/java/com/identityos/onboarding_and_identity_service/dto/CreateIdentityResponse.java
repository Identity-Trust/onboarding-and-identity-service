package com.identityos.onboarding_and_identity_service.dto;

public class CreateIdentityResponse {

    private boolean success;
    private String identityId;
    private String message;

    public CreateIdentityResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public String getIdentityId() {
        return identityId;
    }

    public String getMessage() {
        return message;
    }
}