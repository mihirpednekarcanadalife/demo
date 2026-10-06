package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.PolicyRequest;
import com.example.demo.retirement.exception.InvalidPolicyRequestException;
import com.example.demo.retirement.exception.PolicyNotFoundException;
import com.example.demo.retirement.models.Policy;
import com.example.demo.retirement.models.OwnerType;
import com.example.demo.retirement.repo.PolicyRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final OwnerTypeResolver ownerTypeResolver;

    public PolicyService(PolicyRepository policyRepository, OwnerTypeResolver ownerTypeResolver) {
        this.policyRepository = policyRepository;
        this.ownerTypeResolver = ownerTypeResolver;
    }

    public Policy createPolicy(PolicyRequest request) {
        validateForCreate(request);

        String policyId = (request.policyId() == null || request.policyId().isBlank())
                ? "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : request.policyId().trim();

        if (policyRepository.existsById(policyId)) {
            throw new InvalidPolicyRequestException("Policy already exists: " + policyId);
        }

        // ownerType is stored on the policy record; derive it when the caller does not supply one.
        OwnerType ownerType = request.ownerType() != null
                ? request.ownerType()
                : ownerTypeResolver.resolve(request.owner());

        Instant now = Instant.now();
        Policy policy = new Policy(
                policyId,
                request.riskCommencementDate(),
                request.maturityDate(),
                request.partnerId().trim(),
                request.owner() == null ? null : request.owner().trim(),
                ownerType,
                now,
                now
        );
        return policyRepository.save(policy);
    }

    public Policy updatePolicy(String policyId, PolicyRequest request) {
        if (request == null) {
            throw new InvalidPolicyRequestException("Request body is required");
        }
        Policy existing = getPolicy(policyId);

        if (request.riskCommencementDate() != null) {
            existing.setRiskCommencementDate(request.riskCommencementDate());
        }
        if (request.maturityDate() != null) {
            existing.setMaturityDate(request.maturityDate());
        }
        if (request.partnerId() != null) {
            if (request.partnerId().isBlank()) {
                throw new InvalidPolicyRequestException("partnerId must not be blank");
            }
            existing.setPartnerId(request.partnerId().trim());
        }
        if (request.owner() != null) {
            existing.setOwner(request.owner().trim());
            // Keep the stored classification aligned when the owner changes and none is supplied.
            if (request.ownerType() == null) {
                existing.setOwnerType(ownerTypeResolver.resolve(request.owner()));
            }
        }
        if (request.ownerType() != null) {
            existing.setOwnerType(request.ownerType());
        }

        if (existing.getRiskCommencementDate() != null
                && existing.getMaturityDate() != null
                && !existing.getMaturityDate().isAfter(existing.getRiskCommencementDate())) {
            throw new InvalidPolicyRequestException("maturityDate must be after riskCommencementDate");
        }

        existing.setUpdatedAt(Instant.now());
        return policyRepository.save(existing);
    }

    public Policy getPolicy(String policyId) {
        if (policyId == null || policyId.isBlank()) {
            throw new InvalidPolicyRequestException("policyId is required");
        }
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException(policyId));
    }

    public List<Policy> getPolicies(String partnerId) {
        List<Policy> policies = (partnerId == null || partnerId.isBlank())
                ? policyRepository.findAll()
                : policyRepository.findByPartnerId(partnerId);

        return policies.stream()
                .sorted(Comparator.comparing(Policy::getCreatedAt))
                .toList();
    }

    public void deletePolicy(String policyId) {
        if (!policyRepository.deleteById(policyId)) {
            throw new PolicyNotFoundException(policyId);
        }
    }

    private void validateForCreate(PolicyRequest request) {
        if (request == null) {
            throw new InvalidPolicyRequestException("Request body is required");
        }
        if (request.partnerId() == null || request.partnerId().isBlank()) {
            throw new InvalidPolicyRequestException("partnerId is required");
        }
        if (request.riskCommencementDate() == null) {
            throw new InvalidPolicyRequestException("riskCommencementDate is required");
        }
        if (request.maturityDate() == null) {
            throw new InvalidPolicyRequestException("maturityDate is required");
        }
        if (!request.maturityDate().isAfter(request.riskCommencementDate())) {
            throw new InvalidPolicyRequestException("maturityDate must be after riskCommencementDate");
        }
    }
}