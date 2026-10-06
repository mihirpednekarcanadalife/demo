package com.example.demo.retirement.api;

import com.example.demo.retirement.api.dto.PolicyRequest;
import com.example.demo.retirement.models.Policy;
import com.example.demo.retirement.service.PolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    public ResponseEntity<Policy> createPolicy(@RequestBody PolicyRequest request) {
        Policy created = policyService.createPolicy(request);
        return ResponseEntity
                .created(URI.create("/api/v1/policies/" + created.getPolicyId()))
                .body(created);
    }

    @PutMapping("/{policyId}")
    public Policy updatePolicy(@PathVariable String policyId, @RequestBody PolicyRequest request) {
        return policyService.updatePolicy(policyId, request);
    }

    @GetMapping("/{policyId}")
    public Policy getPolicy(@PathVariable String policyId) {
        return policyService.getPolicy(policyId);
    }

    @GetMapping
    public List<Policy> getPolicies(@RequestParam(required = false) String partnerId) {
        return policyService.getPolicies(partnerId);
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<Void> deletePolicy(@PathVariable String policyId) {
        policyService.deletePolicy(policyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}