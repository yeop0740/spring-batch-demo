package com.heeyeop.springbatch_demo2;

import com.heeyeop.springbatch_demo2.entity.sales.Sales;
import com.heeyeop.springbatch_demo2.entity.sales.SalesRepository;
import com.heeyeop.springbatch_demo2.entity.sales.SalesSum;
import com.heeyeop.springbatch_demo2.entity.sales.SalesSumRepository;
import com.heeyeop.springbatch_demo2.job.ch10.BatchJpaTestConfiguration;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes = {BatchJpaTestConfiguration.class, TestBatchConfiguration.class})
public class BatchJpaUnitTestJobConfigurationTest {
    @Autowired
    private JpaPagingItemReader<SalesSum> reader;
    @Autowired
    private SalesSumRepository salesSumRepository;
    @Autowired
    private SalesRepository salesRepository;

    public static final LocalDate orderDate = LocalDate.of(2019, 10, 6);

    @After
    public void tearDown() throws Exception {
        salesRepository.deleteAllInBatch();
        salesSumRepository.deleteAllInBatch();
    }

    public StepExecution getStepExecution() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("orderDate", orderDate.format(BatchJpaTestConfiguration.FORMATTER))
                .toJobParameters();

        return MetaDataInstanceFactory.createStepExecution(jobParameters);
    }

    @Test
    public void 기간내_Sales가_집계되어_SalesSum이된다() throws Exception {
        // given
        int amount1 = 1000;
        int amount2 = 500;
        int amount3 = 100;

        saveSales(amount1, "1");
        saveSales(amount2, "2");
        saveSales(amount3, "3");

        reader.open(new ExecutionContext());

        // when && then
        assertThat(reader.read().getAmountSum()).isEqualTo(amount1 + amount2 + amount3);
        assertThat(reader.read()).isNull();
    }

    private Sales saveSales(long amount, String orderNo) {
        return salesRepository.save(new Sales(orderDate, amount, orderNo));
    }
}
