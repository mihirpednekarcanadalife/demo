package com.example.demo.retirement.api.dto;

import com.example.demo.retirement.models.Case;

import java.time.LocalDate;

/**
 * Outcome of analysing a policy's maturity date against the one-year retirement horizon.
 *
 * @param policyId          the analysed policy
 * @param maturityDate      the policy maturity date
 * @param evaluationDate    the date the assessment was run (today)
 * @param daysToMaturity    days between {@code evaluationDate} and {@code maturityDate} (negative when already matured)
 * @param maturityDetected  {@code true} when maturity is one year away or less
 * @param caseCreated       {@code true} only when this call created the case; {@code false} when one already existed
 * @param linkedCase        the case linked one-to-one to the policy, or {@code null} when none applies
 * @param message           human readable explanation
 */
public record MaturityAssessmentResponse(
        String policyId,
        LocalDate maturityDate,
        LocalDate evaluationDate,
        long daysToMaturity,
        boolean maturityDetected,
        boolean caseCreated,
        Case linkedCase,
        String message
) {
}