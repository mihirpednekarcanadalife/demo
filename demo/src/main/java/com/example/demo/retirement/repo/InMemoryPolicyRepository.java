package com.example.demo.retirement.repo;

import com.example.demo.retirement.models.Policy;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple thread-safe in-memory store for policies.
 */
@Repository
public class InMemoryPolicyRepository implements PolicyRepository {

    private final Map<String, Policy> store = new ConcurrentHashMap<>();

    @Override
    public Policy save(Policy policy) {
        store.put(policy.getPolicyId(), policy);
        return policy;
    }

    @Override
    public Optional<Policy> findById(String policyId) {
        return Optional.ofNullable(store.get(policyId));
    }

    @Override
    public List<Policy> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public List<Policy> findByPartnerId(String partnerId) {
        return store.values().stream()
                .filter(policy -> Objects.equals(policy.getPartnerId(), partnerId))
                .toList();
    }

    @Override
    public boolean existsById(String policyId) {
        return store.containsKey(policyId);
    }

    @Override
    public boolean deleteById(String policyId) {
        return store.remove(policyId) != null;
    }
}