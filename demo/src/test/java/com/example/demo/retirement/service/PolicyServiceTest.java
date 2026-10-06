package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.PolicyRequest;
import com.example.demo.retirement.exception.InvalidPolicyRequestException;
import com.example.demo.retirement.exception.PolicyNotFoundException;
import com.example.demo.retirement.models.OwnerType;
import com.example.demo.retirement.models.Policy;
import com.example.demo.retirement.models.OwnerType;
import com.example.demo.retirement.repo.InMemoryPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyServiceTest {

    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        policyService = new PolicyService(new InMemoryPolicyRepository(), new OwnerTypeResolver("CLE,CLE-TEAM"));
    }

    private PolicyRequest validRequest(String policyId, String partnerId) {
        return new PolicyRequest(policyId, LocalDate.of(2010, 4, 1), LocalDate.of(2027, 4, 1), partnerId, "advisor-1", null);
    }

    @Test
    void createsPolicyWithGeneratedId() {
        Policy created = policyService.createPolicy(validRequest(null, "PARTNER-1"));

        assertThat(created.getPolicyId()).startsWith("POL-");
        assertThat(created.getPartnerId()).isEqualTo("PARTNER-1");
        assertThat(created.getMaturityDate()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void honoursSuppliedPolicyIdAndRejectsDuplicates() {
        policyService.createPolicy(validRequest("POL-123", "PARTNER-1"));

        assertThatThrownBy(() -> policyService.createPolicy(validRequest("POL-123", "PARTNER-2")))
                .isInstanceOf(InvalidPolicyRequestException.class);
    }

    @Test
    void rejectsMissingPartnerId() {
        assertThatThrownBy(() -> policyService.createPolicy(validRequest(null, " ")))
                .isInstanceOf(InvalidPolicyRequestException.class);
    }

    @Test
    void derivesOwnerTypeWhenNotSupplied() {
        Policy nonCle = policyService.createPolicy(validRequest("POL-NONCLE", "PARTNER-1"));
        Policy cle = policyService.createPolicy(new PolicyRequest(
                "POL-CLE", LocalDate.of(2010, 4, 1), LocalDate.of(2027, 4, 1), "PARTNER-1", "CLE", null));

        assertThat(nonCle.getOwnerType()).isEqualTo(OwnerType.NON_CLE);
        assertThat(cle.getOwnerType()).isEqualTo(OwnerType.CLE);
    }

    @Test
    void honoursExplicitOwnerType() {
        Policy created = policyService.createPolicy(new PolicyRequest(
                "POL-EXPLICIT", LocalDate.of(2010, 4, 1), LocalDate.of(2027, 4, 1), "PARTNER-1", "advisor-1", OwnerType.CLE));

        assertThat(created.getOwnerType()).isEqualTo(OwnerType.CLE);
    }

    @Test
    void rejectsMaturityBeforeRiskCommencement() {
        PolicyRequest bad = new PolicyRequest(null, LocalDate.of(2030, 1, 1), LocalDate.of(2020, 1, 1), "PARTNER-1", "advisor-1", null);

        assertThatThrownBy(() -> policyService.createPolicy(bad))
                .isInstanceOf(InvalidPolicyRequestException.class);
    }

    @Test
    void filtersByPartnerId() {
        policyService.createPolicy(validRequest(null, "PARTNER-1"));
        policyService.createPolicy(validRequest(null, "PARTNER-2"));

        assertThat(policyService.getPolicies("PARTNER-2")).hasSize(1);
        assertThat(policyService.getPolicies(null)).hasSize(2);
    }

    @Test
    void updatesOnlyProvidedFields() {
        Policy created = policyService.createPolicy(validRequest("POL-999", "PARTNER-1"));

        Policy updated = policyService.updatePolicy("POL-999",
                new PolicyRequest(null, null, null, null, "advisor-9", null));

        assertThat(updated.getOwner()).isEqualTo("advisor-9");
        assertThat(updated.getPartnerId()).isEqualTo("PARTNER-1");
        assertThat(updated.getRiskCommencementDate()).isEqualTo(created.getRiskCommencementDate());
    }

    @Test
    void unknownPolicyFails() {
        assertThatThrownBy(() -> policyService.getPolicy("POL-MISSING"))
                .isInstanceOf(PolicyNotFoundException.class);
    }
}