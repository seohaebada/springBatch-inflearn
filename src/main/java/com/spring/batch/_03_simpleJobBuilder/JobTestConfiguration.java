package com.spring.batch._03_simpleJobBuilder;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*
--job.name=testJob
 */
@Configuration
@RequiredArgsConstructor
public class JobTestConfiguration {

    // job 생성
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job testJob() {
        return this.jobBuilderFactory.get("testJob")
                /* step start */
                // simpleJobBuilder.java 로 이동 (SimpleJob 객체)
                // 실행 : SimpleJob을 스프링부트는 자동으로 실행해주는에 어떻게 되는걸까?
                // JobLauncherApplicationRunner 가 실행해준다.
                // execute() : jobLauncher.run(job, parameters);
                // stepHandler.handleStep(step, execution); : 스텝실행

                /*
                1) SimpleJobLauncher 의 run()
                - JobExecution 생성 (마지막에 수행된 JobExecution 객체를 사져옴, 여기서 JobInstance를 가져오는 구문이 있음)
                - SimpleJobRepository 에서 JobInstance, ExecutionContext 생성
                - 위 정보들을 set한 JobExecution이 생성됨
                - job.execute(jobExecution); 를 통해 Job 실행
                - DefaultJobParameterValidator 에서 검증과정을 거침
                - AbstractJob 에서 updateStatus(STARTED) 수행
                -
                 */
                .start(testStep1())
                .next(testStep2())
                .build();
    }

    @Bean
    public Step testStep1() {
        return stepBuilderFactory.get("testStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("step1");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step testStep2() {
        return stepBuilderFactory.get("testStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("step2");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }
}
