package com.uums.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    Job usageAggregationJob(JobRepository jobRepository, Step usageAggregationStep) {
        return new JobBuilder("usageAggregationJob", jobRepository)
                .start(usageAggregationStep)
                .build();
    }

    @Bean
    Step usageAggregationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("usageAggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED, transactionManager)
                .build();
    }
}
