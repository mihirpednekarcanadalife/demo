package com.example.demo.retirement.batch;

import com.example.demo.retirement.api.dto.OwnerDetectionResponse;
import com.example.demo.retirement.models.OwnerType;
import com.example.demo.retirement.service.OwnerDetectionService;
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
 * Step 2: classifies the owner of every {@code MATURITY_DETECTED} case as CLE / NON-CLE,
 * stamps the routing email and advances the case status.
 */
@Component
public class OwnerDetectionTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(OwnerDetectionTasklet.class);

    private final OwnerDetectionService ownerDetectionService;

    public OwnerDetectionTasklet(OwnerDetectionService ownerDetectionService) {
        this.ownerDetectionService = ownerDetectionService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<OwnerDetectionResponse> results = ownerDetectionService.detectOwners();

        long cle = results.stream().filter(r -> r.ownerType() == OwnerType.CLE).count();
        long nonCle = results.size() - cle;

        log.info("Owner detection: {} cases processed ({} CLE, {} NON-CLE)", results.size(), cle, nonCle);

        ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
        stepContext.putInt("casesProcessed", results.size());
        stepContext.putLong("cleOwners", cle);
        stepContext.putLong("nonCleOwners", nonCle);

        contribution.incrementWriteCount(results.size());
        return RepeatStatus.FINISHED;
    }
}