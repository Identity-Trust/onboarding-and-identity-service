package com.identityos.onboarding_and_identity_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.identityos.onboarding_and_identity_service.dto.CreateIdentityRequest;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityResponse;

@Component
public class AuthenticationClient {

    private final RestClient restClient;

    public AuthenticationClient(
            RestClient.Builder builder,
            @Value("${services.authentication.url}") String authenticationUrl) {

        this.restClient = builder
                .baseUrl(authenticationUrl)
                .build();
    }

    public CreateIdentityResponse createIdentity(
            CreateIdentityRequest request) {

        return restClient
                .post()
                .uri("/internal/v1/identities")
                .body(request)
                .retrieve()
                .body(CreateIdentityResponse.class);
    }
}
