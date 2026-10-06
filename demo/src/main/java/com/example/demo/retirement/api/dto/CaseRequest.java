package com.example.demo.retirement.api.dto;

import com.example.demo.retirement.models.CaseStatus;

/**
 * Payload used for both create and update of a case.
 * On update, null fields are left unchanged.
 */
public record CaseRequest(
        String caseName,
        CaseStatus caseStatus,
        String caseSla,
        String owner,
        String description,
        String policyId
) {
}