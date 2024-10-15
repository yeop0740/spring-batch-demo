package com.heeyeop.springbatch_demo2.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class SimpleJobConfiguration {

    @Bean
    public Job simpleJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("simpleJob", jobRepository)
                .start(simpleStep1(jobRepository, transactionManager, null))
                .next(simpleStep2(jobRepository, transactionManager, null))
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager, @Value("#{jobParameters[requestDate]}") String requestDate) {
        return new StepBuilder("simpleStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is Step1");
                    log.info(">>>>> requestDate = {}", requestDate);
                    return RepeatStatus.FINISHED;
//                    throw new IllegalArgumentException("step1에서 실패합니다.");
                }, transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager, @Value("#{jobParameters[requestDate]}") String requestDate) {
        return new StepBuilder("simpleStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is Step2");
                    log.info(">>>>> requestDate = {}", requestDate);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
