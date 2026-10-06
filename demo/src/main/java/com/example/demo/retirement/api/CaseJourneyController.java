package com.example.demo.retirement.api;

import com.example.demo.retirement.api.dto.CaseJourneyResponse;
import com.example.demo.retirement.api.dto.DocumentUploadRequest;
import com.example.demo.retirement.api.dto.MaturityOptionRequest;
import com.example.demo.retirement.service.CaseJourneyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer-facing journey endpoints used by the customer portal.
 */
@RestController
@RequestMapping("/api/v1/cases/{caseId}/journey")
public class CaseJourneyController {

    private final CaseJourneyService caseJourneyService;

    public CaseJourneyController(CaseJourneyService caseJourneyService) {
        this.caseJourneyService = caseJourneyService;
    }

    /** Current stage, available options and outstanding documents. */
    @GetMapping
    public CaseJourneyResponse getJourney(@PathVariable String caseId) {
        return caseJourneyService.getJourney(caseId);
    }

    /** Select a maturity option - moves the case to AWAITING_INFORMATION. */
    @PostMapping("/maturity-option")
    public CaseJourneyResponse selectMaturityOption(@PathVariable String caseId,
                                                    @RequestBody MaturityOptionRequest request) {
        return caseJourneyService.selectMaturityOption(caseId, request == null ? null : request.maturityOption());
    }

    /** Upload a document - accepted immediately and completes the case. */
    @PostMapping("/documents")
    public CaseJourneyResponse uploadDocument(@PathVariable String caseId,
                                              @RequestBody DocumentUploadRequest request) {
        return caseJourneyService.uploadDocument(caseId, request == null ? null : request.document());
    }
}