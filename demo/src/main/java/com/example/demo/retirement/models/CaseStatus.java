package com.example.demo.retirement.models;

public enum CaseStatus {
    NEW,
    MATURITY_DETECTED,
    CLE_OWNER_DETECTED,
    NON_CLE_OWNER_DETECTED,
    MATURITY_PACKAGE_SENT,
    AWAITING_INFORMATION,
    IN_PROGRESS,
    AWAITING_CUSTOMER,
    ON_HOLD,
    COMPLETED,
    CANCELLED
}