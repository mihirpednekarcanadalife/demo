package com.example.demo.retirement.api;

import com.example.demo.retirement.api.dto.MaturityPackageEmailResponse;
import com.example.demo.retirement.service.MaturityPackageEmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Manual trigger for the maturity package email step of the retirement journey workflow.
 */
@RestController
@RequestMapping("/api/v1/cases")
public class MaturityPackageEmailController {

    private final MaturityPackageEmailService maturityPackageEmailService;

    public MaturityPackageEmailController(MaturityPackageEmailService maturityPackageEmailService) {
        this.maturityPackageEmailService = maturityPackageEmailService;
    }

    /**
     * Builds the default welcome email for every owner-detected case and advances it to
     * {@code MATURITY_PACKAGE_SENT}. Idempotent.
     */
    @PostMapping("/maturity-package-email")
    public List<MaturityPackageEmailResponse> sendMaturityPackages() {
        return maturityPackageEmailService.sendMaturityPackages();
    }
}