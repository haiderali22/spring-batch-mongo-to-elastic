package com.hali.spring.springbatchmongotoelastic;


import com.hali.springbatchmongo.config.SpringBatchConfiguration;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Import(SpringBatchConfiguration.class)
public class BatchConfiguration {

    private final  ElasticToMongoConfig elasticToMongoConfig;
    private final MongoOperations mongoTemplate;
    private final ElasticsearchOperations elasticsearchOperations;

    @Bean
    public Job migrationJob(JobRepository jobRepository ,
                            PlatformTransactionManager transactionManager) {

        SimpleJobBuilder jobBuilder =  new JobBuilder("migrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new MyJobExecutionListener())
                .start(buildStep(elasticToMongoConfig.getItems().get(0),
                        jobRepository, transactionManager));

        elasticToMongoConfig.getItems().stream().skip(1).forEach(
                elasticToMongoConfigItem -> {
                    Step step = buildStep(elasticToMongoConfigItem,
                            jobRepository, transactionManager);
                    jobBuilder.next(step);
                });

        return jobBuilder.build();
    }

      public Step buildStep(ElasticToMongoConfigItem elasticToMongoConfigItem,
                          JobRepository jobRepository ,
                          PlatformTransactionManager transactionManager){

        String mongoCollection = elasticToMongoConfigItem.getMongoCollection();
        String elasticsearchIndex = elasticToMongoConfigItem.getElasticIndex();

        return new StepBuilder(mongoCollection + "MigrationStep", jobRepository)
                .<Document,IndexQuery> chunk(10, transactionManager)
                .reader(mongoReader(mongoCollection, elasticToMongoConfigItem.getMongoQuery()))
                .processor(mongoToElasticProcessor())
                .writer(elasticWriter(elasticsearchIndex))
                .build();
    }


    public MongoItemReader<Document> mongoReader(String collectionName , String query ) {
        MongoItemReader<Document> reader = new MongoItemReader<>();
        reader.setTemplate(mongoTemplate);
        reader.setCollection(collectionName);
        reader.setType(Document.class);
        reader.setQuery(query);
        return reader;
    }


    public ItemProcessor<Document, IndexQuery> mongoToElasticProcessor() {
        return new MongoToElasticProcessor();
    }


    public ElasticsearchItemWriter elasticWriter(String indexName) {
        ElasticsearchItemWriter writer = new
                ElasticsearchItemWriter(elasticsearchOperations, indexName);
        return writer;
    }

}
