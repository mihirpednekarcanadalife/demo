package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.CaseJourneyResponse;
import com.example.demo.retirement.api.dto.CaseRequest;
import com.example.demo.retirement.exception.InvalidCaseRequestException;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.models.MaturityOption;
import com.example.demo.retirement.models.RequiredDocument;
import com.example.demo.retirement.repo.CaseRepository;
import com.example.demo.retirement.repo.InMemoryCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseJourneyServiceTest {

    private CaseService caseService;
    private CaseJourneyService journeyService;

    @BeforeEach
    void setUp() {
        CaseRepository caseRepository = new InMemoryCaseRepository();
        caseService = new CaseService(caseRepository);
        journeyService = new CaseJourneyService(caseRepository, caseService);
    }

    private Case givenSentCase(String policyId) {
        return caseService.createCase(new CaseRequest(
                "Maturity detected for policy " + policyId,
                CaseStatus.MATURITY_PACKAGE_SENT, "30d", "advisor-7", "desc", policyId));
    }

    @Test
    void offersAllMaturityOptionsAndDocuments() {
        Case created = givenSentCase("POL-1");

        CaseJourneyResponse journey = journeyService.getJourney(created.getCaseId());

        assertThat(journey.availableOptions())
                .containsExactly(MaturityOption.ANNUITY, MaturityOption.LUMP_SUM, MaturityOption.REINVEST);
        assertThat(journey.requiredDocuments())
                .containsExactly(RequiredDocument.PASSPORT, RequiredDocument.BANK_DETAILS);
        assertThat(journey.outstandingDocuments()).hasSize(2);
    }

    @Test
    void selectingAnOptionMovesCaseToAwaitingInformation() {
        Case created = givenSentCase("POL-2");

        CaseJourneyResponse journey =
                journeyService.selectMaturityOption(created.getCaseId(), MaturityOption.ANNUITY);

        assertThat(journey.retirementCase().getMaturityOption()).isEqualTo(MaturityOption.ANNUITY);
        assertThat(journey.retirementCase().getCaseStatus()).isEqualTo(CaseStatus.AWAITING_INFORMATION);
        assertThat(journey.message()).contains("Passport", "Bank details");
    }

    @Test
    void rejectsMissingOption() {
        Case created = givenSentCase("POL-3");

        assertThatThrownBy(() -> journeyService.selectMaturityOption(created.getCaseId(), null))
                .isInstanceOf(InvalidCaseRequestException.class);
    }

    @Test
    void documentsCannotBeUploadedBeforeAnOptionIsChosen() {
        Case created = givenSentCase("POL-4");

        assertThatThrownBy(() -> journeyService.uploadDocument(created.getCaseId(), RequiredDocument.PASSPORT))
                .isInstanceOf(InvalidCaseRequestException.class)
                .hasMessageContaining("maturity option");
    }

    @Test
    void firstDocumentKeepsCaseAwaitingInformation() {
        Case created = givenSentCase("POL-5");
        journeyService.selectMaturityOption(created.getCaseId(), MaturityOption.LUMP_SUM);

        CaseJourneyResponse journey =
                journeyService.uploadDocument(created.getCaseId(), RequiredDocument.PASSPORT);

        assertThat(journey.uploadedDocuments()).containsExactly(RequiredDocument.PASSPORT);
        assertThat(journey.outstandingDocuments()).containsExactly(RequiredDocument.BANK_DETAILS);
        assertThat(journey.retirementCase().getCaseStatus()).isEqualTo(CaseStatus.AWAITING_INFORMATION);
        assertThat(journey.message()).contains("Bank details");
    }

    @Test
    void caseCompletesOnlyWhenAllDocumentsAreUploaded() {
        Case created = givenSentCase("POL-7");
        journeyService.selectMaturityOption(created.getCaseId(), MaturityOption.ANNUITY);

        journeyService.uploadDocument(created.getCaseId(), RequiredDocument.PASSPORT);
        CaseJourneyResponse journey =
                journeyService.uploadDocument(created.getCaseId(), RequiredDocument.BANK_DETAILS);

        assertThat(journey.uploadedDocuments()).hasSize(2);
        assertThat(journey.outstandingDocuments()).isEmpty();
        assertThat(journey.retirementCase().getCaseStatus()).isEqualTo(CaseStatus.COMPLETED);
        assertThat(journey.message()).contains("ready for retirement");
    }

    @Test
    void reUploadingTheSameDocumentDoesNotComplete() {
        Case created = givenSentCase("POL-8");
        journeyService.selectMaturityOption(created.getCaseId(), MaturityOption.REINVEST);

        journeyService.uploadDocument(created.getCaseId(), RequiredDocument.PASSPORT);
        CaseJourneyResponse journey =
                journeyService.uploadDocument(created.getCaseId(), RequiredDocument.PASSPORT);

        assertThat(journey.uploadedDocuments()).containsExactly(RequiredDocument.PASSPORT);
        assertThat(journey.retirementCase().getCaseStatus()).isEqualTo(CaseStatus.AWAITING_INFORMATION);
    }

    @Test
    void completedCaseCannotChangeItsOption() {
        Case created = givenSentCase("POL-6");
        journeyService.selectMaturityOption(created.getCaseId(), MaturityOption.REINVEST);
        journeyService.uploadDocument(created.getCaseId(), RequiredDocument.PASSPORT);
        journeyService.uploadDocument(created.getCaseId(), RequiredDocument.BANK_DETAILS);

        assertThatThrownBy(() ->
                journeyService.selectMaturityOption(created.getCaseId(), MaturityOption.ANNUITY))
                .isInstanceOf(InvalidCaseRequestException.class)
                .hasMessageContaining("already complete");
    }
}