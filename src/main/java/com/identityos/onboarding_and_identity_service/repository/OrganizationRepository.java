package com.identityos.onboarding_and_identity_service.repository;

import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

        jdbcTemplate.update("""
                INSERT INTO organizations (
                    entity_id, organization_id, organization_name, organization_type,
                    country_code, official_email, official_phone, registration_number,
                    registration_authority, incorporation_date, verification_id_type,
                    verification_id, verification_id_verify_status, website_url, logo_url
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                entityId, organizationId, request.organizationName(),
                request.organizationType(), request.countryCode(), request.officialEmail(),
                request.officialPhone(), request.registrationNumber(), request.registrationAuthority(),
                request.incorporationDate(), request.verificationIdType(), request.verificationId(),
                request.verificationIdVerifyStatus(), request.websiteUrl(), request.logoUrl());
    }
}
