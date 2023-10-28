package com.spring.batch._62_writerAdapter;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class ItemWriterAdapterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job itemWriterAdapterJob() throws Exception {
        return jobBuilderFactory.get("itemWriterAdapterJob")
                .incrementer(new RunIdIncrementer())
                .start(itemWriterAdapterStep1())
                .build();
    }

    @Bean
    public Step itemWriterAdapterStep1() throws Exception {
        return stepBuilderFactory.get("itemWriterAdapterStep1")
                .<String, String>chunk(10)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        return i > 10 ? null : "item" + i;
                    }
                })
                .writer(itemWriterAdapterCustomItemWriter())
                .build();
    }

    /**
     * ItemAdapter 사용하여 메서드 위임
     * @return
     */
    @Bean
    public ItemWriterAdapter itemWriterAdapterCustomItemWriter() {
        ItemWriterAdapter<String>  writer = new ItemWriterAdapter<>();
        writer.setTargetObject(itemWriterAdapterCustomService());
        writer.setTargetMethod("customWrite");
        return  writer;
    }

    @Bean
    public CustomService itemWriterAdapterCustomService() {
        return new CustomService();
    }
}

