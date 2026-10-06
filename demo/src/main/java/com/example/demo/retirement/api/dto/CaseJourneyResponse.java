package com.example.demo.retirement.api.dto;

import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.MaturityOption;
import com.example.demo.retirement.models.RequiredDocument;

import java.util.List;

/**
 * The customer-facing view of where a case sits in the maturity journey.
 *
 * @param retirementCase     the case itself
 * @param availableOptions   maturity options the customer may choose from
 * @param requiredDocuments  every document the customer may be asked for
 * @param uploadedDocuments  documents already supplied
 * @param outstandingDocuments documents still missing
 * @param message            human readable explanation of the current stage
 */
public record CaseJourneyResponse(
        Case retirementCase,
        List<MaturityOption> availableOptions,
        List<RequiredDocument> requiredDocuments,
        List<RequiredDocument> uploadedDocuments,
        List<RequiredDocument> outstandingDocuments,
        String message
) {
}