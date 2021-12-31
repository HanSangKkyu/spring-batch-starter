package com.example.batchdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@EnableBatchProcessing
@SpringBootApplication
public class BatchDemoApplication {

    public static void main(String[] args) {
        log.info("실행은 잘 되었습니다.");
//        SpringApplication.run(BatchDemoApplication.class, args);
//        SpringApplication.run(BatchDemoApplication.class, new String[]{"-name=ssss"}); // 프로그램 arguments를 설정한다. #{jobParameters[name]}에 들어가게 될 것이다.
    SpringApplication.run(BatchDemoApplication.class, new String[]{"-inputPath=/Users/han/Documents/docker/input.csv","-outputPath=/Users/han/Documents/docker/output.csv"});

    }


}
