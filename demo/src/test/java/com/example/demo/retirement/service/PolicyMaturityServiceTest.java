package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.MaturityAssessmentResponse;
import com.example.demo.retirement.api.dto.PolicyRequest;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.repo.CaseRepository;
import com.example.demo.retirement.repo.InMemoryCaseRepository;
import com.example.demo.retirement.repo.InMemoryPolicyRepository;
import com.example.demo.retirement.repo.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyMaturityServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

    private PolicyService policyService;
    private CaseRepository caseRepository;
    private PolicyMaturityService maturityService;

    @BeforeEach
    void setUp() {
        PolicyRepository policyRepository = new InMemoryPolicyRepository();
        caseRepository = new InMemoryCaseRepository();
        policyService = new PolicyService(policyRepository, new OwnerTypeResolver("CLE"));

        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        maturityService = new PolicyMaturityService(policyService, policyRepository, caseRepository, fixedClock);
    }

    private void givenPolicy(String policyId, LocalDate maturityDate) {
        policyService.createPolicy(new PolicyRequest(
                policyId, LocalDate.of(2010, 1, 1), maturityDate, "PARTNER-1", "advisor-1", null));
    }

    @Test
    void createsCaseWhenMaturityIsWithinOneYear() {
        givenPolicy("POL-NEAR", TODAY.plusMonths(6));

        MaturityAssessmentResponse result = maturityService.assessPolicy("POL-NEAR");

        assertThat(result.maturityDetected()).isTrue();
        assertThat(result.caseCreated()).isTrue();
        assertThat(result.linkedCase().getCaseStatus()).isEqualTo(CaseStatus.MATURITY_DETECTED);
        assertThat(result.linkedCase().getPolicyId()).isEqualTo("POL-NEAR");
        assertThat(result.daysToMaturity()).isPositive();
    }

    @Test
    void treatsExactlyOneYearAsDetected() {
        givenPolicy("POL-BOUNDARY", TODAY.plusYears(1));

        assertThat(maturityService.assessPolicy("POL-BOUNDARY").maturityDetected()).isTrue();
    }

    @Test
    void doesNotCreateCaseWhenMaturityIsFurtherAway() {
        givenPolicy("POL-FAR", TODAY.plusYears(1).plusDays(1));

        MaturityAssessmentResponse result = maturityService.assessPolicy("POL-FAR");

        assertThat(result.maturityDetected()).isFalse();
        assertThat(result.caseCreated()).isFalse();
        assertThat(result.linkedCase()).isNull();
        assertThat(caseRepository.findAll()).isEmpty();
    }

    @Test
    void isIdempotentAndNeverDuplicatesCases() {
        givenPolicy("POL-REPEAT", TODAY.plusMonths(3));

        MaturityAssessmentResponse first = maturityService.assessPolicy("POL-REPEAT");
        MaturityAssessmentResponse second = maturityService.assessPolicy("POL-REPEAT");
        MaturityAssessmentResponse third = maturityService.assessPolicy("POL-REPEAT");

        assertThat(first.caseCreated()).isTrue();
        assertThat(second.caseCreated()).isFalse();
        assertThat(third.caseCreated()).isFalse();
        assertThat(second.linkedCase().getCaseId()).isEqualTo(first.linkedCase().getCaseId());
        assertThat(caseRepository.findAll()).hasSize(1);
    }

    @Test
    void assessesAllPoliciesInOneSweep() {
        givenPolicy("POL-A", TODAY.plusMonths(2));
        givenPolicy("POL-B", TODAY.plusYears(5));

        List<MaturityAssessmentResponse> results = maturityService.assessAllPolicies();

        assertThat(results).hasSize(2);
        assertThat(results).filteredOn(MaturityAssessmentResponse::caseCreated).hasSize(1);
        assertThat(caseRepository.findAll()).hasSize(1);
    }

    @Test
    void detectsAlreadyMaturedPolicy() {
        givenPolicy("POL-PAST", TODAY.minusMonths(2));

        MaturityAssessmentResponse result = maturityService.assessPolicy("POL-PAST");

        assertThat(result.maturityDetected()).isTrue();
        assertThat(result.daysToMaturity()).isNegative();
    }
}