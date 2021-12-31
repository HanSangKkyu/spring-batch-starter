package com.example.batchdemo;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.support.ListPreparedStatementSetter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory; // job을 만들 때 사용함
    private final StepBuilderFactory stepBuilderFactory; // step을 만들 때 사용함
    private final DataSource dataSource; // DB에 접근할 때 사용함 application.yml에 설정해야 함
    private final JobListener jobListener; // job이 시작하고 끝날 때 발생할 이벤트를 정의함
    private final StepListener stepListener; // step이 시작하고 끝날 때 발생할 이벤트를 정의함

    private String WILL_BE_INJECTED = null; // 프로그램 arguments로 들어올 값을 대비해서 null을 사용한다.

    @Bean
    public Job job(){
        return jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .listener(jobListener) // 리스터 설정
                .start(flatstep()) // 파일에서 읽어서 파일에 쓰기 step
//                .start(step(WILL_BE_INJECTED))
//                .next(step(WILL_BE_INJECTED)) // 이렇게 연결 가능
//                .next(step(WILL_BE_INJECTED))
//                .next(step(WILL_BE_INJECTED))
                .build();
    }

    @Bean
    @JobScope
    public Step step(@Value("#{jobParameters[name]}") String name){
        log.info("name is {} ",name);
        return  stepBuilderFactory
                .get("userstep3") // 이 값은 유니크 해야 한다. DB에 저장되어서 관리되기 때문에 배치를 실행할 때마다 다른 값을 넣어주어야 한다.
                .allowStartIfComplete(true) // 한번 실행된 step도 재실행 될 수 있게 허용한다. 순서도 중요하다. 초반에 배치해야 한다.
                .listener(stepListener) // 리스터를 달아준다. 초반에 배치해야 한다.
                .<User,User>chunk(1)// 100개의 데이터를 chunk 사이즈 10으로 해서 10번 수행할 수 있습니다.
//                .reader(jdbcCursorItemReader())
                .reader(jdbcCursorItemReader(WILL_BE_INJECTED))// 리더를 설정하고 매개변수를 null로 두면 @Value("#{jobParameters[name]}") String name에 매칭되는 값이 들어갈 것임
                .processor(itemProcessor())// 프로세서 설정
//                .writer(itemWriter()) // DB 접근 없다면 이렇게 가능
                .writer(jdbcBatchItemWriter(dataSource))
                .build();
    }



    @Bean
    @JobScope
    public Step flatstep(){
        return  stepBuilderFactory
                .get("flatstep") // 이 값은 유니크 해야 한다. DB에 저장되어서 관리되기 때문에 배치를 실행할 때마다 다른 값을 넣어주어야 한다.
                .allowStartIfComplete(true) // 한번 실행된 step도 재실행 될 수 있게 허용한다. 순서도 중요하다. 초반에 배치해야 한다.
                .listener(stepListener) // 리스터를 달아준다. 초반에 배치해야 한다.
                .<User,User>chunk(1)// 100개의 데이터를 chunk 사이즈 10으로 해서 10번 수행할 수 있습니다.
                .reader(flatFileItemReader(WILL_BE_INJECTED))// 리더를 설정하고 매개변수를 null로 두면 @Value("#{jobParameters[name]}") String name에 매칭되는 값이 들어갈 것임
                .writer(flatFileItemWriter(WILL_BE_INJECTED))
                .build();
    }


    //    private ItemWriter<User> itemWriter() { // DB 접근 없을 때 사용 가능
//        return items -> log.info("chunk item size : {}, result : {}", items.size(), items.toString());
//
//    }

    @Bean
    public JdbcBatchItemWriter<User> jdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<User>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO ProcessedUser (name) VALUES (:name)")
                .dataSource(dataSource)
                .build();
    }

    //item을 가공하거나, writer로 넘길지 말지 결정 null일경우 writer로 넘어가지 않음
    private ItemProcessor<User, User> itemProcessor() {
        return item -> new User(item.getId(), item.getName().toUpperCase());
    }


    @Bean
    @StepScope
    public JdbcCursorItemReader jdbcCursorItemReader(@Value("#{jobParameters[name]}") String name) {
        log.info("리드 시작됨 {} ", name);

        String sql = "select * from User where name = :name";

        // sql에 들어갈 값을 세팅한다.
        Map<String, Object> namedParameters = new HashMap<String, Object>() {{
            put("name", name);
        }};

        return new JdbcCursorItemReaderBuilder<User>()
                .name("jdbcCursorItemReader")
                .dataSource(dataSource)
//                .sql("select id, name from User;") // parameter 없으면 이거 써도 된다.
                .rowMapper(new BeanPropertyRowMapper<>(User.class))
                .sql(NamedParameterUtils.substituteNamedParameters(sql, new MapSqlParameterSource(namedParameters)))
                .preparedStatementSetter(new ListPreparedStatementSetter(Arrays.asList(NamedParameterUtils.buildValueArray(sql, namedParameters))))
                .build();
    }


    @Bean
    @StepScope
    public FlatFileItemReader<User> flatFileItemReader(@Value("#{jobParameters[inputPath]}") String inputPath){
        return new FlatFileItemReaderBuilder<User>()
                .name("flatFileItemReader")
                .resource(new FileSystemResource(inputPath))
                .delimited().delimiter(",")
                .names(new String[]{"id", "name"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<User>() {{
                    setTargetType(User.class);
                }})
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<User> flatFileItemWriter(@Value("#{jobParameters[outputPath]}") String outputPath){
        return new FlatFileItemWriterBuilder<User>()
                .name("flatFileItemWriter")
                .resource(new FileSystemResource(outputPath))
                .delimited().delimiter(",")
                .names(new String[] {"id", "name"})
                .build();
    }

}