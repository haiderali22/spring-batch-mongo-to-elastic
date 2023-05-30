package com.hali.spring.springbatchmongotoelastic;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
public class ElasticToMongoConfigItem {
    private String mongoCollection;
    private String mongoQuery;
    private String elasticIndex;
}
