package com.identityos.onboarding_and_identity_service.repository;

import com.identityos.onboarding_and_identity_service.dto.RegisterOrganizationRequest;
import com.identityos.onboarding_and_identity_service.dto.ApplicationRegistrationRequest;
import com.identityos.onboarding_and_identity_service.dto.ApplicationResponse;
import com.identityos.onboarding_and_identity_service.dto.OrganizationProfileResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
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
                    verification_id, verification_id_verify_status, website_url, logo_url,
                    status, approval_status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', 'PENDING')
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

    public List<OrganizationProfileResponse> findOrganizations(String approvalStatus) {
        if (approvalStatus == null || approvalStatus.isBlank()) {
            return jdbcTemplate.query("""
                    SELECT organization_id, organization_name, organization_type, country_code,
                           official_email, official_phone, registration_number, registration_authority,
                           verification_id_type, verification_id, verification_id_verify_status,
                           website_url, logo_url, status, approval_status
                    FROM organizations
                    ORDER BY created_at DESC
                    """, organizationRowMapper());
        }

        String sql = """
                SELECT organization_id, organization_name, organization_type, country_code,
                       official_email, official_phone, registration_number, registration_authority,
                       verification_id_type, verification_id, verification_id_verify_status,
                       website_url, logo_url, status, approval_status
                FROM organizations
                WHERE approval_status = ?
                ORDER BY created_at DESC
                """;
        return jdbcTemplate.query(sql, organizationRowMapper(), approvalStatus);
    }

    public void updateApprovalStatus(String organizationId, String decision) {
        String approvalStatus = "APPROVED".equalsIgnoreCase(decision) ? "APPROVED" : "REJECTED";
        String status = "APPROVED".equals(approvalStatus) ? "ACTIVE" : "INACTIVE";
        jdbcTemplate.update("""
                UPDATE organizations
                SET approval_status = ?, status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE organization_id = ?
                """, approvalStatus, status, organizationId);
    }

    public ApplicationResponse insertApplication(String organizationId, ApplicationRegistrationRequest request) {
        UUID entityId = jdbcTemplate.queryForObject("""
                INSERT INTO entities (entity_type, status)
                VALUES ('APPLICATION', 'ACTIVE')
                RETURNING id
                """, UUID.class);
        UUID organizationUuid = jdbcTemplate.queryForObject("""
                SELECT id FROM organizations WHERE organization_id = ?
                """, UUID.class, organizationId);
        String applicationId = "app_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        return jdbcTemplate.queryForObject("""
                INSERT INTO applications (
                    entity_id, organization_id, application_id, application_name,
                    application_type, description, redirect_uri, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'INACTIVE')
                RETURNING id, application_id, application_name, application_type, description,
                          redirect_uri, status, trust_score, created_at
                """, (resultSet, rowNum) -> new ApplicationResponse(
                    resultSet.getString("id"),
                    organizationId,
                    null,
                    resultSet.getString("application_id"),
                    resultSet.getString("application_name"),
                    resultSet.getString("application_type"),
                    resultSet.getString("description"),
                    resultSet.getString("redirect_uri"),
                    resultSet.getString("status"),
                    resultSet.getBigDecimal("trust_score"),
                    resultSet.getTimestamp("created_at").toLocalDateTime()),
                entityId,
                organizationUuid,
                applicationId,
                request.applicationName(),
                request.applicationType(),
                request.description(),
                request.redirectUri());
    }

    public List<ApplicationResponse> findApplications(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            return jdbcTemplate.query("""
                    SELECT a.id, o.organization_id, o.organization_name, a.application_id,
                           a.application_name, a.application_type, a.description, a.redirect_uri,
                           a.status, a.trust_score, a.created_at
                    FROM applications a
                    JOIN organizations o ON o.id = a.organization_id
                    ORDER BY a.created_at DESC
                    """, applicationRowMapper());
        }

        return jdbcTemplate.query("""
                SELECT a.id, o.organization_id, o.organization_name, a.application_id,
                       a.application_name, a.application_type, a.description, a.redirect_uri,
                       a.status, a.trust_score, a.created_at
                FROM applications a
                JOIN organizations o ON o.id = a.organization_id
                WHERE o.organization_id = ?
                ORDER BY a.created_at DESC
                """, applicationRowMapper(), organizationId);
    }

    public void updateApplicationStatus(String applicationId, String decision) {
        String status = "APPROVED".equalsIgnoreCase(decision) || "ACTIVE".equalsIgnoreCase(decision)
                ? "ACTIVE"
                : "SUSPENDED";
        jdbcTemplate.update("""
                UPDATE applications
                SET status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE application_id = ?
                """, status, applicationId);
    }

    private RowMapper<OrganizationProfileResponse> organizationRowMapper() {
        return (resultSet, rowNum) -> new OrganizationProfileResponse(
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
                resultSet.getString("approval_status"));
    }

    private RowMapper<ApplicationResponse> applicationRowMapper() {
        return (resultSet, rowNum) -> new ApplicationResponse(
                resultSet.getString("id"),
                resultSet.getString("organization_id"),
                resultSet.getString("organization_name"),
                resultSet.getString("application_id"),
                resultSet.getString("application_name"),
                resultSet.getString("application_type"),
                resultSet.getString("description"),
                resultSet.getString("redirect_uri"),
                resultSet.getString("status"),
                resultSet.getBigDecimal("trust_score"),
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }
}
