package com.example.demo.retirement.models;

import java.time.Instant;

/**
 * A retirement journey case tracked by the back-office.
 */
public class Case {

    private String caseId;
    private String caseName;
    private CaseStatus caseStatus;
    private String caseSla;
    private String owner;
    private String description;
    /** One-to-one link to {@link Policy#getPolicyId()}. At most one case per policy. */
    private String policyId;
    /** Owner classification, populated by the owner-detection step. */
    private OwnerType ownerType;
    /** Routing mailbox derived from {@link #ownerType}. */
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

    public Case() {
    }

    public Case(String caseId, String caseName, CaseStatus caseStatus, String caseSla,
                String owner, String description, Instant createdAt, Instant updatedAt) {
        this(caseId, caseName, caseStatus, caseSla, owner, description, null, createdAt, updatedAt);
    }

    public Case(String caseId, String caseName, CaseStatus caseStatus, String caseSla,
                String owner, String description, String policyId, Instant createdAt, Instant updatedAt) {
        this.caseId = caseId;
        this.caseName = caseName;
        this.caseStatus = caseStatus;
        this.caseSla = caseSla;
        this.owner = owner;
        this.description = description;
        this.policyId = policyId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public OwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(OwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public CaseStatus getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(CaseStatus caseStatus) {
        this.caseStatus = caseStatus;
    }

    public String getCaseSla() {
        return caseSla;
    }

    public void setCaseSla(String caseSla) {
        this.caseSla = caseSla;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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