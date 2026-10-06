package com.example.demo.retirement.batch;

import com.example.demo.retirement.api.dto.MaturityAssessmentResponse;
import com.example.demo.retirement.service.PolicyMaturityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Step 1: assesses every policy and raises {@code MATURITY_DETECTED} cases for policies
 * maturing within one year.
 */
@Component
public class MaturityAssessmentTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(MaturityAssessmentTasklet.class);

    private final PolicyMaturityService policyMaturityService;

    public MaturityAssessmentTasklet(PolicyMaturityService policyMaturityService) {
        this.policyMaturityService = policyMaturityService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<MaturityAssessmentResponse> results = policyMaturityService.assessAllPolicies();

        long detected = results.stream().filter(MaturityAssessmentResponse::maturityDetected).count();
        long created = results.stream().filter(MaturityAssessmentResponse::caseCreated).count();

        log.info("Maturity assessment: {} policies assessed, {} maturing within one year, {} new cases created",
                results.size(), detected, created);

        ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
        stepContext.putInt("policiesAssessed", results.size());
        stepContext.putLong("maturityDetected", detected);
        stepContext.putLong("casesCreated", created);

        contribution.incrementWriteCount(created);
        return RepeatStatus.FINISHED;
    }
}