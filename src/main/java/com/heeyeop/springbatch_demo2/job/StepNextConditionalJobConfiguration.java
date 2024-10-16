package com.heeyeop.springbatch_demo2.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StepNextConditionalJobConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job stepNextConditionalJob() {
        return new JobBuilder("stepNextConditionalJob", jobRepository)
                .start(conditionalJobStep1())
                .on("FAILED") // exit status == FAILED 인 경우
                .to(conditionalJobStep3()) // step3 로 이동
                .on("*") // step3의 모든 status 에 대해
                .end() // 종료
                .from(conditionalJobStep1()) // step1 에서 시작
                .on("*") // status FAILED 외의 모든 상태에 대해
                .to(conditionalJobStep2()) // step2 로 이동
                .next(conditionalJobStep3()) // step2 이후 step3 으로 이동
                .on("*") // step3 의 결과에 상관 없이
                .end() // 종료한다.
                .end() // job 을 종료한다.
                .build();
    }

    @Bean
    public Step conditionalJobStep1() {
        return new StepBuilder("conditionalJobStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step1");

                    /*
                      ExitStatus를 FAILED로 지정한다.
                      해당 Status를 보고 flow가 진행된다.
                     */
//                    contribution.setExitStatus(ExitStatus.FAILED); // 로직의 결과에 의해 동적으로 결정되는 부분이지만, 예제에선 원하는 값으로 고정하고 사용한다.

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step conditionalJobStep2() {
        return new StepBuilder("conditionalJobStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step2");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step conditionalJobStep3() {
        return new StepBuilder("conditionalJobStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step3");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
