package com.example.demo.retirement.api.dto;

import com.example.demo.retirement.models.OwnerType;

import java.time.LocalDate;

/**
 * Payload used for create and update of a policy.
 * On update, null fields are left unchanged.
 * {@code policyId} is optional on create - one is generated when omitted.
 * {@code ownerType} is optional - it is derived from {@code owner} when omitted.
 */
public record PolicyRequest(
        String policyId,
        LocalDate riskCommencementDate,
        LocalDate maturityDate,
        String partnerId,
        String owner,
        OwnerType ownerType
) {
}