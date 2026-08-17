package com.identityos.onboarding_and_identity_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.identityos.onboarding_and_identity_service.client.AuthenticationClient;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityRequest;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityResponse;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final AuthenticationClient authenticationClient;

    public OnboardingController(
            AuthenticationClient authenticationClient) {
        this.authenticationClient = authenticationClient;
    }

    @PostMapping("/test-authentication")
    public ResponseEntity<CreateIdentityResponse> testAuthentication(
            @RequestBody CreateIdentityRequest request) {

        CreateIdentityResponse response =
                authenticationClient.createIdentity(request);

        return ResponseEntity.ok(response);
    }
}
