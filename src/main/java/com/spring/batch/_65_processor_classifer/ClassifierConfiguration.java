package com.spring.batch._65_processor_classifer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class ClassifierConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job classifierJob() throws Exception {
        return jobBuilderFactory.get("classifierJob")
                .incrementer(new RunIdIncrementer())
                .start(classifierStep1())
                .build();
    }

    @Bean
    public Step classifierStep1() throws Exception {
        return stepBuilderFactory.get("classifierStep1")
                .<ProcessorInfo, ProcessorInfo>chunk(10)
                .reader(new ItemReader<ProcessorInfo>() {
                    int i = 0;
                    @Override
                    public ProcessorInfo read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        ProcessorInfo processorInfo = ProcessorInfo.builder().id(i).build();
                        return i > 3 ? null : processorInfo;
                    }
                })
                .processor(classifierCustomItemProcessor())
                .writer(new ItemWriter<ProcessorInfo>() {
                    @Override
                    public void write(List<? extends ProcessorInfo> items) throws Exception {
                        System.out.println(items);
                    }
                })
                .build();
    }

    @Bean
    public ItemProcessor classifierCustomItemProcessor() {

        /*
           ClassifierCompositeItemProcessor > process()
           // classify() 메서드를 호출한다. (어떤 Processor 을 선택할 것인지의 로직을 구현하여 ItemProcessor 반환)
           -> return processItem(classifier.classify(item), item));
         */
        ClassifierCompositeItemProcessor<ProcessorInfo, ProcessorInfo> processor = new ClassifierCompositeItemProcessor<>();

        ProcessorClassifier<ProcessorInfo, ItemProcessor<?, ? extends ProcessorInfo>> classifier = new ProcessorClassifier();

        Map<Integer, ItemProcessor<ProcessorInfo, ProcessorInfo>> processorMap = new HashMap<>();
        processorMap.put(1, new CustomItemProcessor1());
        processorMap.put(2, new CustomItemProcessor2());
        processorMap.put(3, new CustomItemProcessor3());
        classifier.setProcessorMap(processorMap);

        processor.setClassifier(classifier); // classifier 설정

        return processor;
    }
}
