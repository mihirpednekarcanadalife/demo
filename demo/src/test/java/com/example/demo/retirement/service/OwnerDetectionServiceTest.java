package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.CaseRequest;
import com.example.demo.retirement.api.dto.OwnerDetectionResponse;
import com.example.demo.retirement.api.dto.PolicyRequest;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.models.OwnerType;
import com.example.demo.retirement.models.Policy;
import com.example.demo.retirement.repo.CaseRepository;
import com.example.demo.retirement.repo.InMemoryCaseRepository;
import com.example.demo.retirement.repo.InMemoryPolicyRepository;
import com.example.demo.retirement.repo.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerDetectionServiceTest {

    private CaseRepository caseRepository;
    private PolicyRepository policyRepository;
    private CaseService caseService;
    private PolicyService policyService;
    private OwnerDetectionService ownerDetectionService;

    @BeforeEach
    void setUp() {
        caseRepository = new InMemoryCaseRepository();
        policyRepository = new InMemoryPolicyRepository();
        OwnerTypeResolver resolver = new OwnerTypeResolver("CLE,CLE-TEAM");

        caseService = new CaseService(caseRepository);
        policyService = new PolicyService(policyRepository, resolver);
        ownerDetectionService = new OwnerDetectionService(caseRepository, policyRepository, resolver);
    }

    /** Stores a policy (the DAO record holding ownerType) plus its linked maturity case. */
    private Case givenPolicyAndMaturityCase(String policyId, String owner, OwnerType ownerType) {
        policyService.createPolicy(new PolicyRequest(
                policyId, LocalDate.of(2010, 1, 1), LocalDate.now().plusMonths(3),
                "PARTNER-1", owner, ownerType));

        return caseService.createCase(new CaseRequest(
                "Maturity detected for policy " + policyId,
                CaseStatus.MATURITY_DETECTED, "30d", owner, "desc", policyId));
    }

    @Test
    void readsCleOwnerTypeFromPolicyRecord() {
        givenPolicyAndMaturityCase("POL-1", "advisor-7", OwnerType.CLE);

        List<OwnerDetectionResponse> results = ownerDetectionService.detectOwners();

        assertThat(results).hasSize(1);
        OwnerDetectionResponse result = results.get(0);
        assertThat(result.ownerType()).isEqualTo(OwnerType.CLE);
        assertThat(result.email()).isEqualTo("cle@cle.com");
        assertThat(result.previousStatus()).isEqualTo(CaseStatus.MATURITY_DETECTED);
        assertThat(result.caseStatus()).isEqualTo(CaseStatus.CLE_OWNER_DETECTED);
        assertThat(result.message()).contains("policy record");

        Case stored = caseService.getCase(result.caseId());
        assertThat(stored.getEmail()).isEqualTo("cle@cle.com");
        assertThat(stored.getOwnerType()).isEqualTo(OwnerType.CLE);
        assertThat(stored.getCaseStatus()).isEqualTo(CaseStatus.CLE_OWNER_DETECTED);
    }

    @Test
    void readsNonCleOwnerTypeFromPolicyRecord() {
        givenPolicyAndMaturityCase("POL-2", "CLE", OwnerType.NON_CLE);

        OwnerDetectionResponse result = ownerDetectionService.detectOwners().get(0);

        // The stored policy ownerType wins over the owner string.
        assertThat(result.ownerType()).isEqualTo(OwnerType.NON_CLE);
        assertThat(result.email()).isEqualTo("non-cle@cle.com");
        assertThat(result.caseStatus()).isEqualTo(CaseStatus.NON_CLE_OWNER_DETECTED);
    }

    @Test
    void derivesOwnerTypeWhenPolicyRecordHasNone() {
        policyRepository.save(new Policy("POL-NO-TYPE", LocalDate.of(2010, 1, 1),
                LocalDate.now().plusMonths(3), "PARTNER-1", "CLE-TEAM", null, Instant.now(), Instant.now()));
        caseService.createCase(new CaseRequest("Maturity", CaseStatus.MATURITY_DETECTED,
                "30d", "CLE-TEAM", "desc", "POL-NO-TYPE"));

        OwnerDetectionResponse result = ownerDetectionService.detectOwners().get(0);

        assertThat(result.ownerType()).isEqualTo(OwnerType.CLE);
        assertThat(result.message()).contains("owner value");
    }

    @Test
    void fallsBackToCaseOwnerWhenNoPolicyLinked() {
        caseService.createCase(new CaseRequest("Orphan", CaseStatus.MATURITY_DETECTED,
                "30d", "advisor-9", "desc", null));

        OwnerDetectionResponse result = ownerDetectionService.detectOwners().get(0);

        assertThat(result.ownerType()).isEqualTo(OwnerType.NON_CLE);
        assertThat(result.email()).isEqualTo("non-cle@cle.com");
    }

    @Test
    void ignoresCasesNotInMaturityDetected() {
        caseService.createCase(new CaseRequest("Plain", CaseStatus.NEW, null, "CLE", null, "POL-3"));

        assertThat(ownerDetectionService.detectOwners()).isEmpty();
    }

    @Test
    void isIdempotent() {
        givenPolicyAndMaturityCase("POL-4", "advisor-1", OwnerType.CLE);
        givenPolicyAndMaturityCase("POL-5", "advisor-2", OwnerType.NON_CLE);

        assertThat(ownerDetectionService.detectOwners()).hasSize(2);
        assertThat(ownerDetectionService.detectOwners()).isEmpty();
        assertThat(caseRepository.findAll()).hasSize(2);
    }
}