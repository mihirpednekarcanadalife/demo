package com.example.demo.retirement.api.dto;

import com.example.demo.retirement.models.RequiredDocument;

/**
 * A document upload. {@code fileName} is informational only - the demo does not store content.
 */
public record DocumentUploadRequest(RequiredDocument document, String fileName) {
}