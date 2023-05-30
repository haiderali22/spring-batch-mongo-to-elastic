package com.hali.spring.springbatchmongotoelastic;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final JobLauncher jobLauncher;
    private final Job job;

    private final MongoOperations mongoTemplate;

    @PostMapping("/job/start")
    public void startJob() throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("jobTime",System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
    }

    @PostMapping("/job/data/{collectionName}")
    public void addJobData(Map<String, Object> params,
                           @PathVariable("collectionName") String collectionName){
        Document doc = new Document(params);
        mongoTemplate.getCollection(collectionName).insertOne(doc);
    }
}
