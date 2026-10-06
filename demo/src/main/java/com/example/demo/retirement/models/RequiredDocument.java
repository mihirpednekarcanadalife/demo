package com.example.demo.retirement.models;

/**
 * Documents a customer must supply before a case can complete.
 */
public enum RequiredDocument {

    PASSPORT("Passport"),
    BANK_DETAILS("Bank details");

    private final String label;

    RequiredDocument(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}