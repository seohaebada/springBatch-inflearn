package com.spring.batch._69_skip;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class SkipConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job skipTestJob() throws Exception {
        return jobBuilderFactory.get("skipTestJob")
                .incrementer(new RunIdIncrementer())
                .start(skipTestStep1())
                .build();
    }

    @Bean
    public Step skipTestStep1() throws Exception {
        return stepBuilderFactory.get("skipTestStep1")
                .<String, String>chunk(5)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    @Override
                    public String read() throws SkippableException {
                        i++;
                        if(i == 3) { // 3 skip 되고, chunk Size 만큼 수행 (1,2,4,5,6)
                            throw new SkippableException("skip");
                        }
                        System.out.println("ItemReader : " + i);
                        return i > 20 ? null : String.valueOf(i);
                    }
                })
                .processor(skipTestProcessor())
                .writer(skipTestWriter())
                /* FaultToleranStepBuilder < LimitCheckingItemSkipPolicy */
                .faultTolerant()
//                .noSkip(SkippableException.class) // 아래 설정이 위의 설정을 덮어씀, skip() 설정이 우선
//                .skipPolicy(limitCheckingItemSkipPolicy())
//                .retry(SkippableException.class)
//                .retryLimit(2)
                .skip(SkippableException.class) // 오류 발생시 이 예외는 skip 허용
                .skipLimit(2) // 2번까지 skip 허용 (skip 은 전체 데이터로 센다)
//                .noRollback(SkippableException.class)
                .build();
    }

    @Bean
    public LimitCheckingItemSkipPolicy skipTestLimitCheckingItemSkipPolicy(){

        Map<Class<? extends Throwable>, Boolean> skippableExceptionClasses = new HashMap<>();
        skippableExceptionClasses.put(SkippableException.class, true);

        LimitCheckingItemSkipPolicy limitCheckingItemSkipPolicy = new LimitCheckingItemSkipPolicy(3, skippableExceptionClasses);

        return limitCheckingItemSkipPolicy;
    }

    @Bean
    public SkipItemProcessor skipTestProcessor() {
        SkipItemProcessor processor = new SkipItemProcessor();
        return processor;
    }

    @Bean
    public SkipItemWriter skipTestWriter() {
        SkipItemWriter writer = new SkipItemWriter();
        return writer;
    }
}
