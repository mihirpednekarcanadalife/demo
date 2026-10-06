package com.example.demo.retirement.exception;

public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(String policyId) {
        super("Policy not found: " + policyId);
    }
}