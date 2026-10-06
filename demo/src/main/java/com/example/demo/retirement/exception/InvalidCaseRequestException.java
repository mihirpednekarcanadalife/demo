package com.example.demo.retirement.exception;

public class InvalidCaseRequestException extends RuntimeException {

    public InvalidCaseRequestException(String message) {
        super(message);
    }
}