package com.spring.batch._46_syncrhnizedItemReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class NotSynchronizedConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Bean
    public Job notSynchronizedJob() throws Exception {
        return jobBuilderFactory.get("notSynchronizedJob")
                .incrementer(new RunIdIncrementer())
                .start(notSynchronizedStep1())
                .build();
    }

    @Bean
    public Step notSynchronizedStep1() {
        return stepBuilderFactory.get("notSynchronizedStep1")
                .<Customer, Customer>chunk(100)
                .reader(notSynchronizedCustomItemReader())
                .listener(new ItemReadListener<Customer>() {
                    @Override
                    public void beforeRead() {
                    }
                    @Override
                    public void afterRead(Customer item) {
                        System.out.println("item.getId() : " + item.getId());
                    }
                    @Override
                    public void onReadError(Exception ex) {
                    }
                })
                .writer(notSynchronizedCustomerItemWriter())
                .taskExecutor(notSynchronizedTaskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Customer> notSynchronizedCustomItemReader() {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .fetchSize(100)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
                .sql("select id, firstName, lastName, birthdate from customer")
                .name("NotSafetyReader")
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Customer> notSynchronizedCustomerItemWriter() {
        JdbcBatchItemWriter<Customer> itemWriter = new JdbcBatchItemWriter<>();
        itemWriter.setDataSource(this.dataSource);
        itemWriter.setSql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider());
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    @Bean
    public TaskExecutor notSynchronizedTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("not-safety-thread-");
        return executor;
    }
}
