package com.example.demo.retirement.exception;

public class InvalidPolicyRequestException extends RuntimeException {

    public InvalidPolicyRequestException(String message) {
        super(message);
    }
}