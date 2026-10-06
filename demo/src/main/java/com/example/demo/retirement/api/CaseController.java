package com.example.demo.retirement.api;

import com.example.demo.retirement.api.dto.CaseRequest;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.service.CaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @PostMapping
    public ResponseEntity<Case> createCase(@RequestBody CaseRequest request) {
        Case created = caseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{caseId}")
    public Case updateCase(@PathVariable String caseId, @RequestBody CaseRequest request) {
        return caseService.updateCase(caseId, request);
    }

    @GetMapping("/{caseId}")
    public Case getCase(@PathVariable String caseId) {
        return caseService.getCase(caseId);
    }

    /** Resolves the single case linked to a policy (one-to-one). */
    @GetMapping("/by-policy/{policyId}")
    public Case getCaseByPolicy(@PathVariable String policyId) {
        return caseService.getCaseByPolicyId(policyId);
    }

    @GetMapping
    public List<Case> getAllCases() {
        return caseService.getAllCases();
    }

    @DeleteMapping("/{caseId}")
    public ResponseEntity<Void> deleteCase(@PathVariable String caseId) {
        caseService.deleteCase(caseId);
        return ResponseEntity.noContent().build();
    }
}