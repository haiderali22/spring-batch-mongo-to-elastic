package com.hali.spring.springbatchmongotoelastic;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "app.mongotoelastic")
public class ElasticToMongoConfig {
    private List<ElasticToMongoConfigItem> items;
}
