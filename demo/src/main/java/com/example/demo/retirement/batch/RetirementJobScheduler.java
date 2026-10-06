package com.example.demo.retirement.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Runs {@link RetirementBatchConfig#JOB_NAME} every two minutes.
 *
 * <p>Each trigger supplies a unique {@code launchedAt} parameter so Spring Batch treats it as a
 * fresh job instance rather than rejecting it as already complete.</p>
 */
@Component
@ConditionalOnProperty(name = "retirement.batch.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class RetirementJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetirementJobScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job retirementJourneyJob;

    public RetirementJobScheduler(JobLauncher jobLauncher, Job retirementJourneyJob) {
        this.jobLauncher = jobLauncher;
        this.retirementJourneyJob = retirementJourneyJob;
    }

    /** Every 2 minutes, measured from the end of the previous run to avoid overlap. */
    @Scheduled(fixedDelayString = "${retirement.batch.scheduler.fixed-delay-ms:120000}",
            initialDelayString = "${retirement.batch.scheduler.initial-delay-ms:10000}")
    public void runRetirementJourneyJob() {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addString("launchedAt", Instant.now().toString())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(retirementJourneyJob, parameters);
            log.info("{} finished with status {}", RetirementBatchConfig.JOB_NAME, execution.getStatus());
        } catch (Exception ex) {
            // Never let a failed run kill the scheduler thread.
            log.error("{} failed to run", RetirementBatchConfig.JOB_NAME, ex);
        }
    }
}