package com.example.batchdemo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobListener extends JobExecutionListenerSupport {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        super.beforeJob(jobExecution);
        if(jobExecution.getStatus() == BatchStatus.STARTED) {
            log.info("job start!");
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) { // job을 수행하고 할 행동을 정의한다.
        super.afterJob(jobExecution);

        // 완료된 상태를 확인하기 위해서 DB에서 값을 읽는다.
        if(jobExecution.getStatus() == BatchStatus.COMPLETED){
            log.info("job complete!");

        }
    }


}
