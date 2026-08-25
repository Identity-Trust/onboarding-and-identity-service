package com.identityos.onboarding_and_identity_service.repository;

import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationRequest;
import com.identityos.onboarding_and_identity_service.dto.OrganizationProfileResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrganizationRepository {
    private final JdbcTemplate jdbcTemplate;

    public OrganizationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(RegisterOrganizationRequest request, String organizationId) {
        UUID entityId = request.entityId() == null
                ? jdbcTemplate.queryForObject("""
                    INSERT INTO entities (entity_type, status)
                    VALUES ('ORGANIZATION', 'ACTIVE')
                    RETURNING id
                    """, UUID.class)
                : request.entityId();

        UUID organizationUuid = jdbcTemplate.queryForObject("""
                INSERT INTO organizations (
                    entity_id, organization_id, organization_name, organization_type,
                    country_code, official_email, official_phone, registration_number,
                    registration_authority, incorporation_date, verification_id_type,
                    verification_id, verification_id_verify_status, website_url, logo_url
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                UUID.class,
                entityId, organizationId, request.organizationName(),
                request.organizationType(), request.countryCode(), request.officialEmail(),
                request.officialPhone(), request.registrationNumber(), request.registrationAuthority(),
                request.incorporationDate(), request.verificationIdType(), request.verificationId(),
                request.verificationIdVerifyStatus(), request.websiteUrl(), request.logoUrl());

        insertRepresentative(organizationUuid, request);
        insertAddress(organizationUuid, request);
    }

    private void insertRepresentative(UUID organizationUuid, RegisterOrganizationRequest request) {
        String representativeName = request.representativeName().trim();
        String[] nameParts = representativeName.split("\\s+", 2);
        jdbcTemplate.update("""
                INSERT INTO organization_representatives (
                    organization_id, first_name, last_name, designation, email,
                    email_verified, mobile_number, mobile_verified, emp_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                organizationUuid,
                nameParts[0],
                nameParts.length > 1 ? nameParts[1] : "",
                request.representativeDesignation(),
                request.representativeEmail(),
                false,
                request.representativeMobile(),
                false,
                request.representativeEmployeeId());
    }

    private void insertAddress(UUID organizationUuid, RegisterOrganizationRequest request) {
        jdbcTemplate.update("""
                INSERT INTO organization_addresses (
                    organization_id, address_type, address_line_1, address_line_2,
                    city, district, state, postal_code, country_code, address_proof_ref
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                organizationUuid,
                blankToDefault(request.addressType(), "REGISTERED"),
                blankToDefault(request.addressLine1(), "-"),
                request.addressLine2(),
                blankToDefault(request.city(), "-"),
                request.district(),
                blankToDefault(request.state(), "-"),
                blankToDefault(request.postalCode(), "-"),
                blankToDefault(request.countryCode(), "-"),
                request.addressProofRef());
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public Optional<OrganizationProfileResponse> findByOrganizationId(String organizationId) {
        return jdbcTemplate.query("""
                SELECT organization_id, organization_name, organization_type, country_code,
                       official_email, official_phone, registration_number, registration_authority,
                       verification_id_type, verification_id, verification_id_verify_status,
                       website_url, logo_url, status, approval_status
                FROM organizations
                WHERE organization_id = ?
                """, resultSet -> resultSet.next()
                ? Optional.of(new OrganizationProfileResponse(
                    resultSet.getString("organization_id"),
                    resultSet.getString("organization_name"),
                    resultSet.getString("organization_type"),
                    resultSet.getString("country_code"),
                    resultSet.getString("official_email"),
                    resultSet.getString("official_phone"),
                    resultSet.getString("registration_number"),
                    resultSet.getString("registration_authority"),
                    resultSet.getString("verification_id_type"),
                    resultSet.getString("verification_id"),
                    resultSet.getString("verification_id_verify_status"),
                    resultSet.getString("website_url"),
                    resultSet.getString("logo_url"),
                    resultSet.getString("status"),
                    resultSet.getString("approval_status")))
                : Optional.empty(), organizationId);
    }
}
