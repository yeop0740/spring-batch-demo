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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ProcessorCompositeJobConfiguration {
    public static final String JOB_NAME = "processorCompositeBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job processorCompositeJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .preventRestart()
                .start(processorCompositionStep())
                .build();
    }

    @Bean(BEAN_PREFIX + "step")
    @JobScope
    public Step processorCompositionStep() {
        return new StepBuilder(BEAN_PREFIX + "step", jobRepository)
                .<Teacher, String>chunk(chunkSize, transactionManager)
                .reader(processorCompositionReader())
                .processor(compositeItemProcessor())
                .writer(processorCompositeWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Teacher> processorCompositionReader() {
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name(BEAN_PREFIX + "reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    @Bean
    public CompositeItemProcessor compositeItemProcessor() {
        List<ItemProcessor> delegates = new ArrayList<>(2);
        delegates.add(processorCompositionProcessor1());
        delegates.add(processorCompositionProcessor2());

        CompositeItemProcessor processor = new CompositeItemProcessor();

        processor.setDelegates(delegates);

        return processor;
    }

    public ItemProcessor<Teacher, String> processorCompositionProcessor1() {
        return Teacher::getName;
    }

    public ItemProcessor<String, String> processorCompositionProcessor2() {
        return name -> "안녕하세요. " + name + "입니다.";
    }

    private ItemWriter<String> processorCompositeWriter() {
        return items -> {
            for (String item : items) {
                log.info("Teacher Name={}", item);
            }
        };
    }
}
