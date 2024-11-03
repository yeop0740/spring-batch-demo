package com.heeyeop.springbatch_demo2.job.ch10;

import com.heeyeop.springbatch_demo2.entity.sales.SalesSum;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class BatchJpaTestConfiguration {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final String JOB_NAME = "batchJpaUnitTestJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean
    public Job batchJpaUnitTestJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(batchJpaUnitTestJobStep())
                .build();
    }

    @Bean
    public Step batchJpaUnitTestJobStep() {
        return new StepBuilder("batchJpaUnitTestJobStep", jobRepository)
                .<SalesSum, SalesSum>chunk(chunkSize, transactionManager)
                .reader(batchJpaUnitTestJobReader(null))
                .writer(batchJpaUnitTestJobWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<SalesSum> batchJpaUnitTestJobReader(@Value("#{jobParameters[orderDate]}") String orderDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderDate", orderDate);

        String className = SalesSum.class.getName(); // JPQL 에서 새로운 entity 로 반환하기 위해
        String queryString = String.format(
                "SELECT new %s(s.orderDate, SUM(s.amount)) " +
                "FROM Sales s" +
                "WHERE s.orderDate =:orderDate" +
                "GROUP BY s.orderDate", className);

        return new JpaPagingItemReaderBuilder<SalesSum>()
                .name("batchJpaUnitTestJobReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString(queryString)
                .parameterValues(params)
                .build();
    }

    @Bean
    public JpaItemWriter<SalesSum> batchJpaUnitTestJobWriter() {
        JpaItemWriter<SalesSum> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
