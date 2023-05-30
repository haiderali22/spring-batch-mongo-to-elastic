package com.hali.spring.springbatchmongotoelastic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import java.net.InetSocketAddress;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.host:localhost}")
    private String elasticsearchHost;

    @Value("${spring.data.elasticsearch.port:9200}")
    private int elasticsearchPort;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(InetSocketAddress.createUnresolved(elasticsearchHost ,
                        elasticsearchPort))
                .build();
    }
}