package com.example.demo.retirement.api.dto;

import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.models.OwnerType;

/**
 * The default welcome email generated for a maturing policy, plus the resulting case transition.
 *
 * @param caseId         the processed case
 * @param policyId       the policy linked one-to-one to the case
 * @param recipientEmail mailbox the package was routed to
 * @param ownerType      owner classification driving the routing
 * @param subject        email subject line
 * @param body           email body, including the customer portal login link
 * @param portalLoginUrl the customer portal login link embedded in the body
 * @param previousStatus status before processing
 * @param caseStatus     status after processing
 */
public record MaturityPackageEmailResponse(
        String caseId,
        String policyId,
        String recipientEmail,
        OwnerType ownerType,
        String subject,
        String body,
        String portalLoginUrl,
        CaseStatus previousStatus,
        CaseStatus caseStatus
) {
}