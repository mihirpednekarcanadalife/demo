package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.OwnerDetectionResponse;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.models.OwnerType;
import com.example.demo.retirement.models.Policy;
import com.example.demo.retirement.repo.CaseRepository;
import com.example.demo.retirement.repo.PolicyRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Next workflow step after maturity detection.
 *
 * <p>Fetches all cases, keeps only those in {@link CaseStatus#MATURITY_DETECTED}, reads the
 * {@code ownerType} from the linked {@link Policy} record via the DAO layer, stamps the routing
 * {@code email} and advances the case status accordingly.</p>
 *
 * <p>The step is idempotent: once a case has moved past {@code MATURITY_DETECTED} it is no longer
 * picked up, so re-running never reprocesses or duplicates work.</p>
 */
@Service
public class OwnerDetectionService {

    private final CaseRepository caseRepository;
    private final PolicyRepository policyRepository;
    private final OwnerTypeResolver ownerTypeResolver;

    public OwnerDetectionService(CaseRepository caseRepository,
                                 PolicyRepository policyRepository,
                                 OwnerTypeResolver ownerTypeResolver) {
        this.caseRepository = caseRepository;
        this.policyRepository = policyRepository;
        this.ownerTypeResolver = ownerTypeResolver;
    }

    /**
     * Processes every case currently in {@code MATURITY_DETECTED}.
     *
     * @return one result per processed case, oldest case first
     */
    public List<OwnerDetectionResponse> detectOwners() {
        return caseRepository.findAll().stream()
                .filter(c -> c.getCaseStatus() == CaseStatus.MATURITY_DETECTED)
                .sorted(Comparator.comparing(Case::getCreatedAt))
                .map(this::detectOwner)
                .toList();
    }

    private OwnerDetectionResponse detectOwner(Case retirementCase) {
        CaseStatus previousStatus = retirementCase.getCaseStatus();

        Optional<Policy> policy = Optional.ofNullable(retirementCase.getPolicyId())
                .flatMap(policyRepository::findById);

        // Primary source of truth is the ownerType persisted on the policy record (DAO layer).
        Optional<OwnerType> storedOwnerType = policy.map(Policy::getOwnerType);

        OwnerType ownerType = storedOwnerType
                .orElseGet(() -> ownerTypeResolver.resolve(resolveOwner(policy, retirementCase)));

        String source = storedOwnerType.isPresent() ? "policy record" : "owner value";

        retirementCase.setOwnerType(ownerType);
        retirementCase.setEmail(ownerType.getEmail());
        retirementCase.setCaseStatus(ownerType.getDetectedStatus());
        retirementCase.setUpdatedAt(Instant.now());

        Case saved = caseRepository.save(retirementCase);

        return new OwnerDetectionResponse(
                saved.getCaseId(),
                saved.getPolicyId(),
                saved.getOwner(),
                ownerType,
                saved.getEmail(),
                previousStatus,
                saved.getCaseStatus(),
                "Owner classified as " + ownerType.getLabel() + " from " + source
                        + "; email set to " + saved.getEmail()
                        + " and status advanced to " + saved.getCaseStatus() + "."
        );
    }

    private String resolveOwner(Optional<Policy> policy, Case retirementCase) {
        return policy.map(Policy::getOwner).orElseGet(retirementCase::getOwner);
    }
}