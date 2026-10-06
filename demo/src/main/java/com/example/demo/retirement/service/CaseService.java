package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.CaseRequest;
import com.example.demo.retirement.exception.CaseNotFoundException;
import com.example.demo.retirement.exception.InvalidCaseRequestException;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.repo.CaseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CaseService {

    private final CaseRepository caseRepository;

    public CaseService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public Case createCase(CaseRequest request) {
        validateForCreate(request);

        Instant now = Instant.now();
        String caseId = "CASE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String policyId = (request.policyId() == null || request.policyId().isBlank())
                ? null
                : request.policyId().trim();

        // One-to-one guard: a policy may have at most one case.
        if (policyId != null && !caseRepository.claimPolicyLink(policyId, caseId)) {
            String existingCaseId = caseRepository.findByPolicyId(policyId)
                    .map(Case::getCaseId)
                    .orElse("unknown");
            throw new InvalidCaseRequestException(
                    "A case already exists for policy " + policyId + ": " + existingCaseId);
        }

        Case created = new Case(
                caseId,
                request.caseName().trim(),
                request.caseStatus() == null ? CaseStatus.NEW : request.caseStatus(),
                request.caseSla(),
                request.owner(),
                request.description(),
                policyId,
                now,
                now
        );
        return caseRepository.save(created);
    }

    public Case getCaseByPolicyId(String policyId) {
        if (policyId == null || policyId.isBlank()) {
            throw new InvalidCaseRequestException("policyId is required");
        }
        return caseRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new CaseNotFoundException("for policy " + policyId));
    }

    public Case updateCase(String caseId, CaseRequest request) {
        if (request == null) {
            throw new InvalidCaseRequestException("Request body is required");
        }
        Case existing = getCase(caseId);

        if (request.caseName() != null) {
            if (request.caseName().isBlank()) {
                throw new InvalidCaseRequestException("caseName must not be blank");
            }
            existing.setCaseName(request.caseName().trim());
        }
        if (request.caseStatus() != null) {
            existing.setCaseStatus(request.caseStatus());
        }
        if (request.caseSla() != null) {
            existing.setCaseSla(request.caseSla());
        }
        if (request.owner() != null) {
            existing.setOwner(request.owner());
        }
        if (request.description() != null) {
            existing.setDescription(request.description());
        }
        existing.setUpdatedAt(Instant.now());

        return caseRepository.save(existing);
    }

    public Case getCase(String caseId) {
        if (caseId == null || caseId.isBlank()) {
            throw new InvalidCaseRequestException("caseId is required");
        }
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException(caseId));
    }

    public List<Case> getAllCases() {
        return caseRepository.findAll().stream()
                .sorted(Comparator.comparing(Case::getCreatedAt))
                .toList();
    }

    public void deleteCase(String caseId) {
        if (!caseRepository.deleteById(caseId)) {
            throw new CaseNotFoundException(caseId);
        }
    }

    private void validateForCreate(CaseRequest request) {
        if (request == null) {
            throw new InvalidCaseRequestException("Request body is required");
        }
        if (request.caseName() == null || request.caseName().isBlank()) {
            throw new InvalidCaseRequestException("caseName is required");
        }
    }
}