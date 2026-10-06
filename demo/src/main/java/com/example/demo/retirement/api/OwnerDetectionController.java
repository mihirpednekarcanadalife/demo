package com.example.demo.retirement.api;

import com.example.demo.retirement.api.dto.OwnerDetectionResponse;
import com.example.demo.retirement.service.OwnerDetectionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Triggers the owner-detection step of the retirement journey workflow.
 */
@RestController
@RequestMapping("/api/v1/cases")
public class OwnerDetectionController {

    private final OwnerDetectionService ownerDetectionService;

    public OwnerDetectionController(OwnerDetectionService ownerDetectionService) {
        this.ownerDetectionService = ownerDetectionService;
    }

    /**
     * Processes all {@code MATURITY_DETECTED} cases: classifies the owner as CLE / NON-CLE,
     * sets the routing email and advances the case status. Idempotent.
     */
    @PostMapping("/owner-detection")
    public List<OwnerDetectionResponse> detectOwners() {
        return ownerDetectionService.detectOwners();
    }
}