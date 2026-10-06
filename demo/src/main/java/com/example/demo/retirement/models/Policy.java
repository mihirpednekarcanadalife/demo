package com.example.demo.retirement.models;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A pension/retirement policy held by a partner.
 */
public class Policy {

    private String policyId;
    private LocalDate riskCommencementDate;
    private LocalDate maturityDate;
    private String partnerId;
    private String owner;
    /** Owner classification held against the policy record. */
    private OwnerType ownerType;
    private Instant createdAt;
    private Instant updatedAt;

    public Policy() {
    }

    public Policy(String policyId,
                  LocalDate riskCommencementDate,
                  LocalDate maturityDate,
                  String partnerId,
                  String owner,
                  Instant createdAt,
                  Instant updatedAt) {
        this(policyId, riskCommencementDate, maturityDate, partnerId, owner, null, createdAt, updatedAt);
    }

    public Policy(String policyId,
                  LocalDate riskCommencementDate,
                  LocalDate maturityDate,
                  String partnerId,
                  String owner,
                  OwnerType ownerType,
                  Instant createdAt,
                  Instant updatedAt) {
        this.policyId = policyId;
        this.riskCommencementDate = riskCommencementDate;
        this.maturityDate = maturityDate;
        this.partnerId = partnerId;
        this.owner = owner;
        this.ownerType = ownerType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public LocalDate getRiskCommencementDate() {
        return riskCommencementDate;
    }

    public void setRiskCommencementDate(LocalDate riskCommencementDate) {
        this.riskCommencementDate = riskCommencementDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public OwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(OwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}