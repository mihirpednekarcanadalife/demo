package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.CaseRequest;
import com.example.demo.retirement.exception.CaseNotFoundException;
import com.example.demo.retirement.exception.InvalidCaseRequestException;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.repo.InMemoryCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseServiceTest {

    private CaseService caseService;

    @BeforeEach
    void setUp() {
        caseService = new CaseService(new InMemoryCaseRepository());
    }

    @Test
    void createsCaseWithGeneratedIdAndDefaultStatus() {
        Case created = caseService.createCase(new CaseRequest("Retirement review", null, "5d", "advisor-1", "desc", null));

        assertThat(created.getCaseId()).startsWith("CASE-");
        assertThat(created.getCaseStatus()).isEqualTo(CaseStatus.NEW);
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(caseService.getAllCases()).hasSize(1);
    }

    @Test
    void rejectsCreateWithoutName() {
        assertThatThrownBy(() -> caseService.createCase(new CaseRequest(" ", null, null, null, null, null)))
                .isInstanceOf(InvalidCaseRequestException.class);
    }

    @Test
    void rejectsSecondCaseForSamePolicy() {
        caseService.createCase(new CaseRequest("First", null, null, null, null, "POL-1"));

        assertThatThrownBy(() -> caseService.createCase(new CaseRequest("Second", null, null, null, null, "POL-1")))
                .isInstanceOf(InvalidCaseRequestException.class)
                .hasMessageContaining("POL-1");
    }

    @Test
    void resolvesCaseByPolicyId() {
        Case created = caseService.createCase(new CaseRequest("Linked", null, null, null, null, "POL-2"));

        assertThat(caseService.getCaseByPolicyId("POL-2").getCaseId()).isEqualTo(created.getCaseId());
    }

    @Test
    void updatesOnlyProvidedFields() {
        Case created = caseService.createCase(new CaseRequest("Original", CaseStatus.NEW, "5d", "advisor-1", "desc", null));

        Case updated = caseService.updateCase(created.getCaseId(),
                new CaseRequest(null, CaseStatus.IN_PROGRESS, null, null, null, null));

        assertThat(updated.getCaseName()).isEqualTo("Original");
        assertThat(updated.getCaseSla()).isEqualTo("5d");
        assertThat(updated.getCaseStatus()).isEqualTo(CaseStatus.IN_PROGRESS);
    }

    @Test
    void getUnknownCaseFails() {
        assertThatThrownBy(() -> caseService.getCase("CASE-UNKNOWN"))
                .isInstanceOf(CaseNotFoundException.class);
    }

    @Test
    void deletesCase() {
        Case created = caseService.createCase(new CaseRequest("Temp", null, null, null, null, null));
        caseService.deleteCase(created.getCaseId());

        assertThat(caseService.getAllCases()).isEmpty();
    }
}