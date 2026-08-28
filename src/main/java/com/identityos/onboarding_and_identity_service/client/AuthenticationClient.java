package com.identityos.onboarding_and_identity_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.identityos.onboarding_and_identity_service.dto.CreateIdentityRequest;
import com.identityos.onboarding_and_identity_service.dto.CreateIdentityResponse;
import org.springframework.http.MediaType;

import java.time.Duration;

@Component
public class AuthenticationClient {

    private final RestClient restClient;

    public AuthenticationClient(
            RestClient.Builder builder,
            @Value("${services.authentication.url}") String authenticationUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));

        this.restClient = builder
                .baseUrl(authenticationUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public CreateIdentityResponse createIdentity(
        CreateIdentityRequest request,
        String authorization) {

    return restClient
            .post()
            .uri("/internal/v1/identities")
            .header("Authorization", authorization)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(CreateIdentityResponse.class);
    }

}
