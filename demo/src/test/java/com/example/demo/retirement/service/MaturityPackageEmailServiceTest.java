package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.CaseRequest;
import com.example.demo.retirement.api.dto.MaturityPackageEmailResponse;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.repo.CaseRepository;
import com.example.demo.retirement.repo.InMemoryCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaturityPackageEmailServiceTest {

    private static final String PORTAL_URL = "http://localhost:5173/?tab=portal";

    private CaseRepository caseRepository;
    private CaseService caseService;
    private MaturityPackageEmailService emailService;

    @BeforeEach
    void setUp() {
        caseRepository = new InMemoryCaseRepository();
        caseService = new CaseService(caseRepository);
        emailService = new MaturityPackageEmailService(caseRepository, PORTAL_URL);
    }

    private Case givenCase(String policyId, String owner, CaseStatus status) {
        return caseService.createCase(new CaseRequest(
                "Maturity detected for policy " + policyId, status, "30d", owner, "desc", policyId));
    }

    @Test
    void buildsWelcomeEmailWithPortalLinkAndAdvancesStatus() {
        givenCase("POL-1", "advisor-7", CaseStatus.CLE_OWNER_DETECTED);

        List<MaturityPackageEmailResponse> results = emailService.sendMaturityPackages();

        assertThat(results).hasSize(1);
        MaturityPackageEmailResponse result = results.get(0);

        assertThat(result.subject()).isEqualTo("Your retirement options - policy POL-1");
        assertThat(result.body())
                .contains("Dear advisor-7")
                .contains("POL-1")
                .contains("Welcome to the next step of your retirement journey")
                .contains(PORTAL_URL);
        assertThat(result.portalLoginUrl()).isEqualTo(PORTAL_URL);
        assertThat(result.previousStatus()).isEqualTo(CaseStatus.CLE_OWNER_DETECTED);
        assertThat(result.caseStatus()).isEqualTo(CaseStatus.MATURITY_PACKAGE_SENT);

        assertThat(caseService.getCase(result.caseId()).getCaseStatus())
                .isEqualTo(CaseStatus.MATURITY_PACKAGE_SENT);
    }

    @Test
    void processesBothOwnerDetectedStatuses() {
        givenCase("POL-CLE", "CLE", CaseStatus.CLE_OWNER_DETECTED);
        givenCase("POL-NONCLE", "advisor-42", CaseStatus.NON_CLE_OWNER_DETECTED);

        List<MaturityPackageEmailResponse> results = emailService.sendMaturityPackages();

        assertThat(results).hasSize(2);
        assertThat(results)
                .allSatisfy(r -> assertThat(r.caseStatus()).isEqualTo(CaseStatus.MATURITY_PACKAGE_SENT));
    }

    @Test
    void ignoresCasesThatAreNotOwnerDetected() {
        givenCase("POL-NEW", "advisor-1", CaseStatus.NEW);
        givenCase("POL-MATURITY", "advisor-2", CaseStatus.MATURITY_DETECTED);
        givenCase("POL-DONE", "advisor-3", CaseStatus.COMPLETED);

        assertThat(emailService.sendMaturityPackages()).isEmpty();
    }

    @Test
    void isIdempotent() {
        givenCase("POL-REPEAT", "CLE", CaseStatus.CLE_OWNER_DETECTED);

        assertThat(emailService.sendMaturityPackages()).hasSize(1);
        assertThat(emailService.sendMaturityPackages()).isEmpty();
        assertThat(caseRepository.findAll()).hasSize(1);
    }

    @Test
    void fallsBackToGenericGreetingWhenOwnerMissing() {
        givenCase("POL-NO-OWNER", null, CaseStatus.NON_CLE_OWNER_DETECTED);

        assertThat(emailService.sendMaturityPackages().get(0).body()).contains("Dear Customer");
    }
}