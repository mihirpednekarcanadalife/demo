package com.example.demo.retirement.exception;

public class CaseNotFoundException extends RuntimeException {

    public CaseNotFoundException(String caseId) {
        super("Case not found: " + caseId);
    }
}