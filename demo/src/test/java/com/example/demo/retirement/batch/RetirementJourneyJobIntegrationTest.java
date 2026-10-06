package com.example.demo.retirement.batch;

import com.example.demo.retirement.api.dto.CaseRequest;
import com.example.demo.retirement.api.dto.PolicyRequest;
import com.example.demo.retirement.models.Case;
import com.example.demo.retirement.models.CaseStatus;
import com.example.demo.retirement.models.OwnerType;
import com.example.demo.retirement.service.CaseService;
import com.example.demo.retirement.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
class RetirementJourneyJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private CaseService caseService;

    private JobParameters uniqueParameters() {
        return new JobParametersBuilder()
                .addString("launchedAt", Instant.now().toString() + "-" + System.nanoTime())
                .toJobParameters();
    }

    @Test
    void runsBothStepsAndAdvancesMaturingPolicyToOwnerDetected() throws Exception {
        policyService.createPolicy(new PolicyRequest(
                "POL-BATCH-1", LocalDate.of(2010, 1, 1), LocalDate.now().plusMonths(5),
                "PARTNER-BATCH", "advisor-batch", OwnerType.CLE));

        JobExecution execution = jobLauncherTestUtils.launchJob(uniqueParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getStepExecutions())
                .extracting("stepName")
                .containsExactly("maturityAssessmentStep", "ownerDetectionStep", "maturityPackageEmailStep");

        // Step 1 created the case, step 2 classified it, step 3 emailed the package.
        Case linked = caseService.getCaseByPolicyId("POL-BATCH-1");
        assertThat(linked.getOwnerType()).isEqualTo(OwnerType.CLE);
        assertThat(linked.getEmail()).isEqualTo("cle@cle.com");
        assertThat(linked.getCaseStatus()).isEqualTo(CaseStatus.MATURITY_PACKAGE_SENT);
    }

    @Test
    void leavesDistantMaturityUntouched() throws Exception {
        policyService.createPolicy(new PolicyRequest(
                "POL-BATCH-FAR", LocalDate.of(2010, 1, 1), LocalDate.now().plusYears(9),
                "PARTNER-BATCH", "advisor-batch", OwnerType.NON_CLE));

        JobExecution execution = jobLauncherTestUtils.launchJob(uniqueParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(caseService.getAllCases())
                .noneMatch(c -> "POL-BATCH-FAR".equals(c.getPolicyId()));
    }

    @Test
    void isSafeToRunRepeatedly() throws Exception {
        policyService.createPolicy(new PolicyRequest(
                "POL-BATCH-REPEAT", LocalDate.of(2010, 1, 1), LocalDate.now().plusMonths(2),
                "PARTNER-BATCH", "advisor-batch", OwnerType.NON_CLE));

        jobLauncherTestUtils.launchJob(uniqueParameters());
        JobExecution second = jobLauncherTestUtils.launchJob(uniqueParameters());

        assertThat(second.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(caseService.getAllCases())
                .filteredOn(c -> "POL-BATCH-REPEAT".equals(c.getPolicyId()))
                .hasSize(1);
    }

    @Test
    void maturityPackageEmailStepCanRunInIsolation() {
        caseService.createCase(new CaseRequest("Standalone email", CaseStatus.NON_CLE_OWNER_DETECTED,
                "30d", "advisor-42", "desc", "POL-BATCH-EMAIL"));

        JobExecution execution = jobLauncherTestUtils.launchStep("maturityPackageEmailStep", uniqueParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(caseService.getCaseByPolicyId("POL-BATCH-EMAIL").getCaseStatus())
                .isEqualTo(CaseStatus.MATURITY_PACKAGE_SENT);
    }

    @Test
    void ownerDetectionStepCanRunInIsolation() {
        caseService.createCase(new CaseRequest("Standalone", CaseStatus.MATURITY_DETECTED,
                "30d", "CLE", "desc", "POL-BATCH-STANDALONE"));

        JobExecution execution = jobLauncherTestUtils.launchStep("ownerDetectionStep", uniqueParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(caseService.getCaseByPolicyId("POL-BATCH-STANDALONE").getCaseStatus())
                .isEqualTo(CaseStatus.CLE_OWNER_DETECTED);
    }
}