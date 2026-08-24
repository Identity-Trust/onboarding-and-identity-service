package com.identityos.onboarding_and_identity_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class KeycloakAdminClient {
    private final RestClient restClient;
    private final String realm;
    private final String adminRealm;
    private final String clientId;
    private final String username;
    private final String password;

    public KeycloakAdminClient(
            RestClient.Builder builder,
            @Value("${keycloak.admin.base-url}") String baseUrl,
            @Value("${keycloak.admin.realm}") String realm,
            @Value("${keycloak.admin.admin-realm}") String adminRealm,
            @Value("${keycloak.admin.client-id}") String clientId,
            @Value("${keycloak.admin.username}") String username,
            @Value("${keycloak.admin.password}") String password) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.realm = realm;
        this.adminRealm = adminRealm;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    public boolean createOrganizationAdmin(String organizationId, String representativeName, String email) {
        String token = requestAdminToken();
        String userId = createUser(token, organizationId, representativeName, email);
        try {
            assignOrganizationAdminRole(token, userId);
        } catch (RuntimeException exception) {
            deleteUser(token, userId);
            throw exception;
        }

        try {
            sendVerificationEmail(token, userId);
            return true;
        } catch (RuntimeException exception) {
            // SMTP is optional in local development; the user remains provisioned.
            System.err.println("Keycloak verification email was not sent: " + exception.getMessage());
            return false;
        }
    }

    private String requestAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", username);
        form.add("password", password);

        JsonNode response = restClient.post()
            .uri("/realms/" + adminRealm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
                .retrieve()
                .body(JsonNode.class);
        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("Keycloak admin token was not returned");
        }
        return response.get("access_token").asText();
    }

    private String createUser(String token, String organizationId, String representativeName, String email) {
        String[] nameParts = representativeName.trim().split("\\s+", 2);
        Map<String, Object> user = Map.of(
                "username", organizationId,
                "email", email,
                "emailVerified", false,
                "enabled", true,
                "firstName", nameParts[0],
                "lastName", nameParts.length > 1 ? nameParts[1] : "",
                "attributes", Map.of("organization_id", List.of(organizationId)),
                "requiredActions", List.of("VERIFY_EMAIL", "UPDATE_PASSWORD"));

        var response = restClient.post()
                .uri("/admin/realms/" + realm + "/users")
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .toBodilessEntity();
        String location = response.getHeaders().getFirst("Location");
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Keycloak did not return the created user location");
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private void assignOrganizationAdminRole(String token, String userId) {
        JsonNode role;
        try {
            role = getOrganizationAdminRole(token);
        } catch (HttpClientErrorException.NotFound exception) {
            restClient.post()
                .uri("/admin/realms/" + realm + "/roles")
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "name", "ORGANISATION_ADMIN",
                    "description", "Administrator for a single organization"))
                .retrieve()
                .toBodilessEntity();
            role = getOrganizationAdminRole(token);
        }
        restClient.post()
                .uri("/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(role))
                .retrieve()
                .toBodilessEntity();
    }

    private JsonNode getOrganizationAdminRole(String token) {
        return restClient.get()
                .uri("/admin/realms/" + realm + "/roles/ORGANISATION_ADMIN")
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(JsonNode.class);
    }

    private void sendVerificationEmail(String token, String userId) {
        restClient.put()
                .uri("/admin/realms/" + realm + "/users/" + userId + "/execute-actions-email")
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of("VERIFY_EMAIL", "UPDATE_PASSWORD"))
                .retrieve()
                .toBodilessEntity();
    }

    private void deleteUser(String token, String userId) {
        restClient.delete()
                .uri("/admin/realms/" + realm + "/users/" + userId)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .toBodilessEntity();
    }
}
