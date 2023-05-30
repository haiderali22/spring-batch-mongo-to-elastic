package com.hali.spring.springbatchmongotoelastic;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(exclude = {BatchAutoConfiguration.class})
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {DefaultBatchConfiguration.class}))

public class SpringBatchMongoToElasticApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchMongoToElasticApplication.class, args);
    }

}
