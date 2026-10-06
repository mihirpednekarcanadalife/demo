package com.example.demo.retirement.repo;

import com.example.demo.retirement.models.Case;

import java.util.List;
import java.util.Optional;

public interface CaseRepository {

    Case save(Case retirementCase);

    Optional<Case> findById(String caseId);

    Optional<Case> findByPolicyId(String policyId);

    List<Case> findAll();

    /**
     * Atomically reserves the one-to-one slot for {@code policyId}.
     *
     * @return {@code true} when the link was claimed, {@code false} when a case already exists for the policy.
     */
    boolean claimPolicyLink(String policyId, String caseId);

    boolean deleteById(String caseId);
}