package com.example.demo.retirement.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Retirement journey batch job.
 *
 * <pre>
 * step1: maturityAssessmentStep   -&gt; policyMaturityService.assessAllPolicies()
 * step2: ownerDetectionStep       -&gt; ownerDetectionService.detectOwners()
 * step3: maturityPackageEmailStep -&gt; maturityPackageEmailService.sendMaturityPackages()
 * </pre>
 *
 * <p>All steps are idempotent, so the job is safe to run repeatedly on a schedule.</p>
 */
@Configuration
public class RetirementBatchConfig {

    public static final String JOB_NAME = "retirementJourneyJob";

    @Bean
    public Step maturityAssessmentStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       MaturityAssessmentTasklet maturityAssessmentTasklet) {
        return new StepBuilder("maturityAssessmentStep", jobRepository)
                .tasklet(maturityAssessmentTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step ownerDetectionStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   OwnerDetectionTasklet ownerDetectionTasklet) {
        return new StepBuilder("ownerDetectionStep", jobRepository)
                .tasklet(ownerDetectionTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step maturityPackageEmailStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         MaturityPackageEmailTasklet maturityPackageEmailTasklet) {
        return new StepBuilder("maturityPackageEmailStep", jobRepository)
                .tasklet(maturityPackageEmailTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job retirementJourneyJob(JobRepository jobRepository,
                                    Step maturityAssessmentStep,
                                    Step ownerDetectionStep,
                                    Step maturityPackageEmailStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(maturityAssessmentStep)
                .next(ownerDetectionStep)
                .next(maturityPackageEmailStep)
                .build();
    }
}