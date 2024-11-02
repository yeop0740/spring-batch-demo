package com.heeyeop.springbatch_demo2.job.ch09;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TransactionWriterJobConfiguration {
    public static final String JOB_NAME = "transactionWriterBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job transactionWriterJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .preventRestart()
                .start(transactionWriterStep())
                .build();
    }

    @Bean(BEAN_PREFIX + "step")
    @JobScope
    public Step transactionWriterStep() {
        return new StepBuilder(BEAN_PREFIX + "step", jobRepository)
                .<Teacher, Teacher>chunk(chunkSize, transactionManager)
                .reader(transactionWriterReader())
                .writer(transactionWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Teacher> transactionWriterReader() {
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name(BEAN_PREFIX + "reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    private ItemWriter<Teacher> transactionWriter() {
        return items -> {
            log.info(">>>>>>>>>>> Item Write");
            for (Teacher item : items) {
                log.info("teacher={}, student size={}", item.getName(), item.getStudents().size());
            }
        };
    }
}
