package com.example.batch;

import com.example.batch.chunk.BonusCalcProcessor;
import com.example.batch.domain.Bonus;
import com.example.batch.domain.Emp;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    public BatchConfig(JobBuilderFactory jobBuilderFactory,
                       StepBuilderFactory stepBuilderFactory,
                       SqlSessionFactory sqlSessionFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Bean
    public MyBatisCursorItemReader<Emp> reader() {
        final MyBatisCursorItemReader<Emp> reader = new MyBatisCursorItemReader<>();
        reader.setQueryId("findAll");
        reader.setSqlSessionFactory(sqlSessionFactory);
        return reader;

    }

    @Bean
    public BonusCalcProcessor processor() {
        return new BonusCalcProcessor();
    }

    @Bean
    public MyBatisBatchItemWriter<Bonus> writer() {
        final MyBatisBatchItemWriter<Bonus> writer = new MyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("insert");
        return writer;
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                System.out.println("before job");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                System.out.println("after job");
            }
        };
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory
                .get("step1")
                .<Emp, Bonus>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job job1() {
        return jobBuilderFactory
                .get("job1")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(step1())
                .end()
                .build();
    }

}
