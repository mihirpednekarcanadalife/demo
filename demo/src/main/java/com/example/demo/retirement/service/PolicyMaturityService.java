package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.MaturityAssessmentResponse;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.models.Policy;
import com.example.demo.retirement.repo.CaseRepository;
import com.example.demo.retirement.repo.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Analyses policies and raises a {@link CaseStatus#MATURITY_DETECTED} case when the policy
 * matures within one year from today.
 *
 * <p>The {@code Policy -> Case} relationship is strictly one-to-one: re-running the assessment
 * never produces a duplicate case.</p>
 */
@Service
public class PolicyMaturityService {

    private static final String SLA_MATURITY_DETECTED = "30d";

    private final PolicyService policyService;
    private final PolicyRepository policyRepository;
    private final CaseRepository caseRepository;
    private final Clock clock;

    @Autowired
    public PolicyMaturityService(PolicyService policyService,
                                 PolicyRepository policyRepository,
                                 CaseRepository caseRepository) {
        this(policyService, policyRepository, caseRepository, Clock.systemDefaultZone());
    }

    PolicyMaturityService(PolicyService policyService,
                          PolicyRepository policyRepository,
                          CaseRepository caseRepository,
                          Clock clock) {
        this.policyService = policyService;
        this.policyRepository = policyRepository;
        this.caseRepository = caseRepository;
        this.clock = clock;
    }

    /**
     * Assesses a single policy by id.
     */
    public MaturityAssessmentResponse assessPolicy(String policyId) {
        return assess(policyService.getPolicy(policyId));
    }

    /**
     * Assesses every stored policy, oldest first.
     */
    public List<MaturityAssessmentResponse> assessAllPolicies() {
        return policyRepository.findAll().stream()
                .sorted(Comparator.comparing(Policy::getCreatedAt))
                .map(this::assess)
                .toList();
    }

    private MaturityAssessmentResponse assess(Policy policy) {
        LocalDate today = LocalDate.now(clock);
        LocalDate maturityDate = policy.getMaturityDate();
        LocalDate oneYearHorizon = today.plusYears(1);

        long daysToMaturity = maturityDate == null
                ? Long.MAX_VALUE
                : ChronoUnit.DAYS.between(today, maturityDate);

        boolean maturityDetected = maturityDate != null && !maturityDate.isAfter(oneYearHorizon);

        if (!maturityDetected) {
            return new MaturityAssessmentResponse(
                    policy.getPolicyId(), maturityDate, today, daysToMaturity,
                    false, false, null,
                    "Maturity date is more than one year away; no case raised.");
        }

        // Idempotency: a case may already be linked to this policy.
        var existing = caseRepository.findByPolicyId(policy.getPolicyId());
        if (existing.isPresent()) {
            return new MaturityAssessmentResponse(
                    policy.getPolicyId(), maturityDate, today, daysToMaturity,
                    true, false, existing.get(),
                    "Maturity already detected; existing case reused.");
        }

        Instant now = Instant.now(clock);
        Case candidate = new Case(
                "CASE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "Maturity detected for policy " + policy.getPolicyId(),
                CaseStatus.MATURITY_DETECTED,
                SLA_MATURITY_DETECTED,
                policy.getOwner(),
                "Policy " + policy.getPolicyId() + " for partner " + policy.getPartnerId()
                        + " matures on " + maturityDate + " (" + daysToMaturity + " days). "
                        + "Start the retirement journey and reinvestment planning.",
                policy.getPolicyId(),
                now,
                now
        );

        // Atomically claim the one-to-one slot; loser of a race reuses the winning case.
        if (!caseRepository.claimPolicyLink(policy.getPolicyId(), candidate.getCaseId())) {
            Case winner = caseRepository.findByPolicyId(policy.getPolicyId()).orElseThrow();
            return new MaturityAssessmentResponse(
                    policy.getPolicyId(), maturityDate, today, daysToMaturity,
                    true, false, winner,
                    "Maturity already detected; existing case reused.");
        }

        Case created = caseRepository.save(candidate);
        return new MaturityAssessmentResponse(
                policy.getPolicyId(), maturityDate, today, daysToMaturity,
                true, true, created,
                "Maturity detected within one year; case created.");
    }
}