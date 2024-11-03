package com.heeyeop.springbatch_demo2;

import com.heeyeop.springbatch_demo2.entity.sales.Sales;
import com.heeyeop.springbatch_demo2.entity.sales.SalesRepository;
import com.heeyeop.springbatch_demo2.entity.sales.SalesSum;
import com.heeyeop.springbatch_demo2.entity.sales.SalesSumRepository;
import com.heeyeop.springbatch_demo2.job.ch10.BatchJpaTestConfiguration;
import org.junit.After;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBatchTest
@SpringBootTest(classes = {BatchJpaTestConfiguration.class, TestBatchConfiguration.class})
public class BatchIntegrationTestJobConfigurationNewTest {
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

        JobParameters jobParameters = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                .addString("orderDate", orderDate.format(BatchJpaTestConfiguration.FORMATTER))
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

    @Test
    public void 중복파라미터_회피를위한_유니크파라미터() throws Exception {
        // given
        LocalDate orderDate = LocalDate.of(2019, 10, 6);
        int amount1 = 1000;
        int amount2 = 500;
        int amount3 = 100;

        salesRepository.save(new Sales(orderDate, amount1, "1"));
        salesRepository.save(new Sales(orderDate, amount2, "2"));
        salesRepository.save(new Sales(orderDate, amount3, "3"));

        JobParameters jobParameters1 = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                .addString("orderDate", orderDate.format(BatchJpaTestConfiguration.FORMATTER))
                .toJobParameters();

        JobParameters jobParameters2 = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                .addString("orderDate", orderDate.format(BatchJpaTestConfiguration.FORMATTER))
                .toJobParameters();
        // when
        JobExecution jobExecution1 = jobLauncherTestUtils.launchJob(jobParameters1);
        JobExecution jobExecution2 = jobLauncherTestUtils.launchJob(jobParameters2);

        // then
        String uniqueOrderDate1 = jobParameters1.getString("orderDate");
        String uniqueOrderDate2 = jobParameters2.getString("orderDate");
        assertThat(uniqueOrderDate1).isEqualTo(uniqueOrderDate2);
        assertThat(jobExecution1.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution2.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}
