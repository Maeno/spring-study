package com.example.batch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {SnapshotJobLauncherTestUtils.class,BatchConfig.class, MyBatisConfig.class})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:schema.sql","classpath:data.sql"})
public class BatchConfigTest {

    @Autowired
    @Qualifier(value = "jobLauncherTestUtils")
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void testLaunchJob() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus(), is(BatchStatus.COMPLETED));
    }

}

@Component(value = "jobLauncherTestUtils")
class SnapshotJobLauncherTestUtils extends JobLauncherTestUtils {

    @Autowired
    @Qualifier(value = "job1")
    @Override
    public void setJob(Job job) {
        super.setJob(job);
    }
}
