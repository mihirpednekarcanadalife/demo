package com.example.demo.retirement.api.dto;

import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.models.OwnerType;

/**
 * Outcome of classifying a maturity-detected case owner as CLE or NON-CLE.
 *
 * @param caseId         the processed case
 * @param policyId       the policy linked one-to-one to the case
 * @param owner          the raw owner value that was classified
 * @param ownerType      resolved {@link OwnerType}
 * @param email          routing mailbox written onto the case
 * @param previousStatus status before processing
 * @param caseStatus     status after processing
 * @param message        human readable explanation
 */
public record OwnerDetectionResponse(
        String caseId,
        String policyId,
        String owner,
        OwnerType ownerType,
        String email,
        CaseStatus previousStatus,
        CaseStatus caseStatus,
        String message
) {
}