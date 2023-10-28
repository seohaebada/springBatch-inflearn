package com.spring.batch._59_writer_xml;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class XMLWriterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Bean
    public Job xmlWriterJob() throws Exception {
        return jobBuilderFactory.get("xmlWriterJob")
                .incrementer(new RunIdIncrementer())
                .start(xmlWriterStep1())
                .build();
    }

    @Bean
    public Step xmlWriterStep1() throws Exception {
        return stepBuilderFactory.get("xmlWriterStep1")
                .<Customer, Customer>chunk(10)
                .reader(xmlWriterCustomItemReader())
                .writer(xmlWriterCustomItemWriter())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Customer> xmlWriterCustomItemReader() {

        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setFetchSize(10);
        reader.setRowMapper(new CustomerRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");
        queryProvider.setWhereClause("where firstname like :firstname");

        Map<String, Order> sortKeys = new HashMap<>(1);

        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("firstname", "A%");

        reader.setParameterValues(parameters);

        return reader;
    }

    @Bean
    public StaxEventItemWriter xmlWriterCustomItemWriter() {
        return new StaxEventItemWriterBuilder<Customer>()
                .name("customersWriter")
                .marshaller(xmlWriterItemMarshaller())
                .resource(new FileSystemResource("customer.xml"))
                .rootTagName("customer")
                .overwriteOutput(true)
                .build();

    }

    @Bean
    public XStreamMarshaller xmlWriterItemMarshaller() {
        Map<String, Class<?>> aliases = new HashMap<>();
        aliases.put("customer", Customer.class); // root
        aliases.put("id", Long.class); // 그 이하부터
        aliases.put("firstName", String.class);
        aliases.put("lastName", String.class);
        aliases.put("birthdate", Date.class);
        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xStreamMarshaller.setAliases(aliases);
        return xStreamMarshaller;
    }


}
