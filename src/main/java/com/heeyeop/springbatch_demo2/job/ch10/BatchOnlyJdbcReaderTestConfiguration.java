package com.heeyeop.springbatch_demo2.job.ch10;

import com.heeyeop.springbatch_demo2.entity.sales.SalesSum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class BatchOnlyJdbcReaderTestConfiguration {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final String JOB_NAME = "batchOnlyJdbcReaderTestJob";

    private final DataSource dataSource;

    private int chunkSize;

    @Value("${chunkSize:1000}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<SalesSum> batchOnlyJdbcReaderTestJobReader(@Value("#{jobParameters[orderDate]}") String orderDate) throws Exception{
        Map<String, Object> params = new HashMap<>();
        params.put("orderDate", LocalDate.parse(orderDate, FORMATTER));

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("order_date, sum(amount) as amount_sum");
        queryProvider.setFromClause("from sales");
        queryProvider.setWhereClause("where order_date =:orderDate");
        queryProvider.setGroupClause("group by order_date");
        queryProvider.setSortKey("order_date");

        return new JdbcPagingItemReaderBuilder<SalesSum>()
                .name("batchOblyJdbcReaderTestJobReader")
                .pageSize(chunkSize)
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(SalesSum.class))
                .queryProvider(queryProvider.getObject())
                .parameterValues(params)
                .build();
    }
}
