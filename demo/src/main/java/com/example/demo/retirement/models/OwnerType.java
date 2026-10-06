package com.example.demo.retirement.models;

/**
 * Classification of a case owner.
 */
public enum OwnerType {

    CLE("CLE", "cle@cle.com", CaseStatus.CLE_OWNER_DETECTED),
    NON_CLE("NON-CLE", "non-cle@cle.com", CaseStatus.NON_CLE_OWNER_DETECTED);

    private final String label;
    private final String email;
    private final CaseStatus detectedStatus;

    OwnerType(String label, String email, CaseStatus detectedStatus) {
        this.label = label;
        this.email = email;
        this.detectedStatus = detectedStatus;
    }

    public String getLabel() {
        return label;
    }

    /** Routing mailbox for this owner type. */
    public String getEmail() {
        return email;
    }

    /** Case status applied once this owner type is detected. */
    public CaseStatus getDetectedStatus() {
        return detectedStatus;
    }
}