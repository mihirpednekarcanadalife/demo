package com.example.demo.retirement.batch;

import com.example.demo.retirement.api.dto.MaturityPackageEmailResponse;
import com.example.demo.retirement.models.OwnerType;
import com.example.demo.retirement.service.MaturityPackageEmailService;
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
 * Step 3: builds the default welcome email (with the customer portal login link) for every
 * owner-detected case and advances it to {@code MATURITY_PACKAGE_SENT}.
 */
@Component
public class MaturityPackageEmailTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(MaturityPackageEmailTasklet.class);

    private final MaturityPackageEmailService maturityPackageEmailService;

    public MaturityPackageEmailTasklet(MaturityPackageEmailService maturityPackageEmailService) {
        this.maturityPackageEmailService = maturityPackageEmailService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<MaturityPackageEmailResponse> results = maturityPackageEmailService.sendMaturityPackages();

        long cle = results.stream().filter(r -> r.ownerType() == OwnerType.CLE).count();
        long nonCle = results.size() - cle;

        log.info("Maturity package email: {} packages sent ({} CLE, {} NON-CLE)",
                results.size(), cle, nonCle);
        results.forEach(result ->
                log.debug("Case {} -> {} | subject: {}", result.caseId(), result.recipientEmail(), result.subject()));

        ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
        stepContext.putInt("packagesSent", results.size());
        stepContext.putLong("clePackages", cle);
        stepContext.putLong("nonClePackages", nonCle);

        contribution.incrementWriteCount(results.size());
        return RepeatStatus.FINISHED;
    }
}