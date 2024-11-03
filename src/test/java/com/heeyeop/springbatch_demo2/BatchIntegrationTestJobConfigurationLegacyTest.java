package com.heeyeop.springbatch_demo2;

import com.heeyeop.springbatch_demo2.entity.sales.Sales;
import com.heeyeop.springbatch_demo2.entity.sales.SalesRepository;
import com.heeyeop.springbatch_demo2.entity.sales.SalesSum;
import com.heeyeop.springbatch_demo2.entity.sales.SalesSumRepository;
import com.heeyeop.springbatch_demo2.job.ch10.BatchJpaTestConfiguration;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;

import static com.heeyeop.springbatch_demo2.job.ch10.BatchJpaTestConfiguration.FORMATTER;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={BatchJpaTestConfiguration.class, TestBatchLegacyConfiguration.class})
public class BatchIntegrationTestJobConfigurationLegacyTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private SalesSumRepository salesSumRepository;

    @After
    public void tearDown() throws Exception {
        salesRepository.deleteAllInBatch();
        salesSumRepository.deleteAllInBatch();
    }

    @Test
    public void 기간내_Sales가_집계되어_SalesSum이된다() throws Exception {
        // given
        LocalDate orderDate = LocalDate.of(2019, 10, 6);
        int amount1 = 1000;
        int amount2 = 500;
        int amount3 = 100;

        salesRepository.save(new Sales(orderDate, amount1, "1"));
        salesRepository.save(new Sales(orderDate, amount2, "2"));
        salesRepository.save(new Sales(orderDate, amount3, "3"));

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("orderDate", orderDate.format(FORMATTER))
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        List<SalesSum> salesSums = salesSumRepository.findAll();
        assertThat(salesSums.size()).isEqualTo(1);
        assertThat(salesSums.get(0).getOrderDate()).isEqualTo(orderDate);
        assertThat(salesSums.get(0).getAmountSum()).isEqualTo(amount1 + amount2 + amount3);
    }
}
