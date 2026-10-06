package com.example.demo.retirement.service;

import com.example.demo.retirement.api.dto.MaturityPackageEmailResponse;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.repo.CaseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Third workflow step.
 *
 * <p>Builds the default welcome email - including a link to the customer portal login page - for
 * every case whose owner has been detected, then advances the case to
 * {@link CaseStatus#MATURITY_PACKAGE_SENT}.</p>
 *
 * <p>Idempotent: only {@code CLE_OWNER_DETECTED} and {@code NON_CLE_OWNER_DETECTED} cases are
 * picked up, so a case is never emailed twice.</p>
 */
@Service
public class MaturityPackageEmailService {

    /** Statuses eligible for the maturity package email. */
    public static final Set<CaseStatus> EMAIL_READY_STATUSES =
            EnumSet.of(CaseStatus.CLE_OWNER_DETECTED, CaseStatus.NON_CLE_OWNER_DETECTED);

    private final CaseRepository caseRepository;
    private final String portalLoginUrl;

    public MaturityPackageEmailService(
            CaseRepository caseRepository,
            @Value("${retirement.portal.login-url:http://localhost:5173/?tab=portal}") String portalLoginUrl) {
        this.caseRepository = caseRepository;
        this.portalLoginUrl = portalLoginUrl;
    }

    /**
     * Generates and "sends" the welcome email for every owner-detected case.
     *
     * @return one result per processed case, oldest case first
     */
    public List<MaturityPackageEmailResponse> sendMaturityPackages() {
        return caseRepository.findAll().stream()
                .filter(c -> EMAIL_READY_STATUSES.contains(c.getCaseStatus()))
                .sorted(Comparator.comparing(Case::getCreatedAt))
                .map(this::sendMaturityPackage)
                .toList();
    }

    private MaturityPackageEmailResponse sendMaturityPackage(Case retirementCase) {
        CaseStatus previousStatus = retirementCase.getCaseStatus();

        String subject = buildSubject(retirementCase);
        String body = buildBody(retirementCase);

        retirementCase.setCaseStatus(CaseStatus.MATURITY_PACKAGE_SENT);
        retirementCase.setUpdatedAt(Instant.now());
        Case saved = caseRepository.save(retirementCase);

        return new MaturityPackageEmailResponse(
                saved.getCaseId(),
                saved.getPolicyId(),
                saved.getEmail(),
                saved.getOwnerType(),
                subject,
                body,
                portalLoginUrl,
                previousStatus,
                saved.getCaseStatus()
        );
    }

    String buildSubject(Case retirementCase) {
        String policyId = retirementCase.getPolicyId() == null ? "-" : retirementCase.getPolicyId();
        return "Your retirement options - policy " + policyId;
    }

    String buildBody(Case retirementCase) {
        String owner = retirementCase.getOwner() == null || retirementCase.getOwner().isBlank()
                ? "Customer"
                : retirementCase.getOwner();
        String policyId = retirementCase.getPolicyId() == null ? "-" : retirementCase.getPolicyId();

        return """
                Dear %s,

                Welcome to the next step of your retirement journey. Our records show that your \
                pension policy %s is approaching its maturity date, so we have prepared your \
                retirement options package.

                In your customer portal you can:
                  - review the current value and projection of your pension fund
                  - see the retirement options available to you
                  - track the progress of your case

                Sign in to the customer portal here: %s

                You will be asked to sign in with your username and password to continue.

                Kind regards,
                The Retirement Team
                """.formatted(owner, policyId, portalLoginUrl);
    }
}