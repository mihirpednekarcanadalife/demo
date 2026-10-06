package com.example.demo.retirement.repo;

import com.example.demo.retirement.models.Case;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple thread-safe in-memory store for cases.
 *
 * <p>{@code policyIndex} enforces the one-to-one {@code Policy -> Case} relationship.</p>
 */
@Repository
public class InMemoryCaseRepository implements CaseRepository {

    private final Map<String, Case> store = new ConcurrentHashMap<>();
    private final Map<String, String> policyIndex = new ConcurrentHashMap<>();

    @Override
    public Case save(Case retirementCase) {
        store.put(retirementCase.getCaseId(), retirementCase);
        if (retirementCase.getPolicyId() != null) {
            policyIndex.put(retirementCase.getPolicyId(), retirementCase.getCaseId());
        }
        return retirementCase;
    }

    @Override
    public Optional<Case> findById(String caseId) {
        return Optional.ofNullable(store.get(caseId));
    }

    @Override
    public Optional<Case> findByPolicyId(String policyId) {
        if (policyId == null) {
            return Optional.empty();
        }
        String caseId = policyIndex.get(policyId);
        return caseId == null ? Optional.empty() : findById(caseId);
    }

    @Override
    public List<Case> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public boolean claimPolicyLink(String policyId, String caseId) {
        return policyIndex.putIfAbsent(policyId, caseId) == null;
    }

    @Override
    public boolean deleteById(String caseId) {
        Case removed = store.remove(caseId);
        if (removed == null) {
            return false;
        }
        if (removed.getPolicyId() != null) {
            policyIndex.remove(removed.getPolicyId(), caseId);
        }
        return true;
    }
}