package com.example.batchdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StepListener extends StepExecutionListenerSupport {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        if(stepExecution.getStatus() == BatchStatus.STARTED) {
            log.info("chunkStep start!");
        }
    }
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if(stepExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("chunkStep end");
        }
        return new ExitStatus("stepListener exit");
    }
}

