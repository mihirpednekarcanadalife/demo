package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.CaseJourneyResponse;
import com.example.demo.retirement.exception.InvalidCaseRequestException;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.models.MaturityOption;
import com.example.demo.retirement.models.RequiredDocument;
import com.example.demo.retirement.repo.CaseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Drives the customer-facing part of the retirement journey:
 *
 * <pre>
 * MATURITY_PACKAGE_SENT --select option--&gt; AWAITING_INFORMATION --upload document--&gt; COMPLETED
 * </pre>
 */
@Service
public class CaseJourneyService {

    public static final List<MaturityOption> AVAILABLE_OPTIONS = List.of(MaturityOption.values());
    public static final List<RequiredDocument> REQUIRED_DOCUMENTS = List.of(RequiredDocument.values());

    private final CaseRepository caseRepository;
    private final CaseService caseService;

    public CaseJourneyService(CaseRepository caseRepository, CaseService caseService) {
        this.caseRepository = caseRepository;
        this.caseService = caseService;
    }

    /** Current journey state for a case. */
    public CaseJourneyResponse getJourney(String caseId) {
        return describe(caseService.getCase(caseId));
    }

    /**
     * Records the customer's maturity option choice and moves the case to
     * {@link CaseStatus#AWAITING_INFORMATION}.
     */
    public CaseJourneyResponse selectMaturityOption(String caseId, MaturityOption option) {
        if (option == null) {
            throw new InvalidCaseRequestException(
                    "maturityOption is required and must be one of " + AVAILABLE_OPTIONS);
        }

        Case retirementCase = caseService.getCase(caseId);

        if (retirementCase.getCaseStatus() == CaseStatus.COMPLETED) {
            throw new InvalidCaseRequestException(
                    "Case " + caseId + " is already complete and cannot be changed");
        }

        retirementCase.setMaturityOption(option);
        retirementCase.setCaseStatus(CaseStatus.AWAITING_INFORMATION);
        retirementCase.setUpdatedAt(Instant.now());

        return describe(caseRepository.save(retirementCase));
    }

    /**
     * Accepts a customer document. The case completes only once <em>every</em> required
     * document has been supplied.
     */
    public CaseJourneyResponse uploadDocument(String caseId, RequiredDocument document) {
        if (document == null) {
            throw new InvalidCaseRequestException(
                    "document is required and must be one of " + REQUIRED_DOCUMENTS);
        }

        Case retirementCase = caseService.getCase(caseId);

        if (retirementCase.getMaturityOption() == null) {
            throw new InvalidCaseRequestException(
                    "Select a maturity option for case " + caseId + " before uploading documents");
        }

        // Documents are auto-accepted in this demo.
        retirementCase.addUploadedDocument(document);

        boolean allDocumentsReceived =
                retirementCase.getUploadedDocuments().containsAll(REQUIRED_DOCUMENTS);

        retirementCase.setCaseStatus(
                allDocumentsReceived ? CaseStatus.COMPLETED : CaseStatus.AWAITING_INFORMATION);
        retirementCase.setUpdatedAt(Instant.now());

        return describe(caseRepository.save(retirementCase));
    }

    private CaseJourneyResponse describe(Case retirementCase) {
        List<RequiredDocument> uploaded = List.copyOf(retirementCase.getUploadedDocuments());
        List<RequiredDocument> outstanding = Arrays.stream(RequiredDocument.values())
                .filter(document -> !uploaded.contains(document))
                .toList();

        return new CaseJourneyResponse(
                retirementCase,
                AVAILABLE_OPTIONS,
                REQUIRED_DOCUMENTS,
                uploaded,
                outstanding,
                buildMessage(retirementCase, outstanding)
        );
    }

    private String buildMessage(Case retirementCase, List<RequiredDocument> outstanding) {
        return switch (retirementCase.getCaseStatus()) {
            case COMPLETED -> "Your retirement option is confirmed and your documents are accepted. "
                    + "You are ready for retirement.";
            case AWAITING_INFORMATION -> outstanding.isEmpty()
                    ? "All documents received."
                    : "We still need: " + outstanding.stream().map(RequiredDocument::getLabel).toList();
            default -> "Choose how you would like to take your retirement benefits.";
        };
    }
}