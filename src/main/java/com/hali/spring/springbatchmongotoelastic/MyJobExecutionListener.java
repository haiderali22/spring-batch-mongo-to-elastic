package com.hali.spring.springbatchmongotoelastic;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

@RequiredArgsConstructor
public class MyJobExecutionListener implements JobExecutionListener {

  private final JobExplorer jobExplorer;
  private final String jobName;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    ExecutionContext jobContext = jobExecution.getExecutionContext();
    Long lastExecutionTime = getLastJobParameter();

    jobContext.putLong("lastJobTime", lastExecutionTime);
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    ExecutionContext jobContext = jobExecution.getExecutionContext();
    jobContext.putLong("lastJobTime", jobContext.getLong("jobTime",0L));
    jobContext.remove("jobTime");
  }

  public long getLastJobParameter() {

    List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, 1);
    if (!jobInstances.isEmpty()) {
      JobInstance jobInstance = jobInstances.get(0);
      List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
      if (!jobExecutions.isEmpty()) {
        JobExecution jobExecution = jobExecutions.get(0);
        JobParameters jobParameters = jobExecution.getJobParameters();
        return jobParameters.getLong("lastJobTime", 0L);
      }
    }

    // Return a default value if the job parameter is not found
    return 0L;
  }

}