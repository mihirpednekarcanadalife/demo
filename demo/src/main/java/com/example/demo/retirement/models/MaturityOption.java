package com.example.demo.retirement.models;

/**
 * Retirement maturity options a customer can choose from.
 */
public enum MaturityOption {

    ANNUITY("Annuity"),
    LUMP_SUM("Lump sum"),
    REINVEST("Reinvest");

    private final String label;

    MaturityOption(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}