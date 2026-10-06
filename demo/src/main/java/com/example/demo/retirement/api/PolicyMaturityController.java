package com.example.demo.retirement.api;

import com.example.demo.retirement.api.dto.MaturityAssessmentResponse;
import com.example.demo.retirement.service.PolicyMaturityService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Processes policies to detect maturity within one year and raise cases.
 */
@RestController
@RequestMapping("/api/v1/policies")
public class PolicyMaturityController {

    private final PolicyMaturityService policyMaturityService;

    public PolicyMaturityController(PolicyMaturityService policyMaturityService) {
        this.policyMaturityService = policyMaturityService;
    }

    /** Assess one policy. Idempotent: never creates a duplicate case. */
    @PostMapping("/{policyId}/maturity-assessment")
    public MaturityAssessmentResponse assessPolicy(@PathVariable String policyId) {
        return policyMaturityService.assessPolicy(policyId);
    }

    /** Assess every stored policy in one sweep. */
    @PostMapping("/maturity-assessment")
    public List<MaturityAssessmentResponse> assessAllPolicies() {
        return policyMaturityService.assessAllPolicies();
    }
}