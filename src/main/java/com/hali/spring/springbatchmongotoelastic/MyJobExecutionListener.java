package com.hali.spring.springbatchmongotoelastic;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.data.mongodb.core.query.Criteria;

public class MyJobExecutionListener implements JobExecutionListener {

  @Override
  public void beforeJob(JobExecution jobExecution) {
    ExecutionContext jobContext = jobExecution.getExecutionContext();
    Long lastExecutionTime = jobContext.getLong("lastExecutionTime" , 0L);

    jobContext.putLong("lastExecutionTime", lastExecutionTime);
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    // Perform actions after the job ends
  }

}