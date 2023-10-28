package com.spring.batch._75_listener_skip;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class SkipListenerConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final CustomSkipListener customSkipListener;

    @Bean
    public Job skipListenerJob() throws Exception {
        return jobBuilderFactory.get("skipListenerJob")
                .incrementer(new RunIdIncrementer())
                .start(skipListenerStep1())
                .build();
    }

    @Bean
    public Step skipListenerStep1() throws Exception {
        return stepBuilderFactory.get("skipListenerStep1")
                .<Integer, String>chunk(10)
                .reader(skipListenerListItemReader())
                .processor(new ItemProcessor<Integer, String>() {
                    @Override
                    public String process(Integer item) throws Exception {
                        /*
                        ItemReader 에서 3이 제외되서 들어온다.
                        item 4 에서 예외 발생 - skip

                         */
                        if (item == 4) {
                            throw new CustomSkipException("process skipped");
                        }
                        System.out.println("process : " + item);
                        return "item" + item;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(List<? extends String> items) throws Exception {
                        for (String item : items) {
                            /*
                            item 3, 4 가 제외한 item 전달됨
                            item5 일때 skip
                             */
                            if (item.equals("item5")) {
                                throw new CustomSkipException("write skipped");
                            }
                            System.out.println("write : " + item);
                        }
                    }
                })
                .faultTolerant()
                .skip(CustomSkipException.class)
                .skipLimit(3)
                /**
                 * itemReader ~ itemWriter 한번 처리 되고 수행됨
                 * 1) onSkipInProcess (item: 4)
                 * 2) onSkipRead (item: 3)
                 * 3) onSkipWriter (item 5)
                 */
                .listener(customSkipListener)
                .build();
    }

    @Bean
    public ItemReader<Integer> skipListenerListItemReader() {
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        return new LinkedListItemReader<>(list);
    }
}
