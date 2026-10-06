package com.example.demo.retirement.repo;

import com.example.demo.retirement.models.Policy;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository {

    Policy save(Policy policy);

    Optional<Policy> findById(String policyId);

    List<Policy> findAll();

    List<Policy> findByPartnerId(String partnerId);

    boolean existsById(String policyId);

    boolean deleteById(String policyId);
}