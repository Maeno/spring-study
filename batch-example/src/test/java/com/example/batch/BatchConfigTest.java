package com.example.batch;

import com.example.batch.domain.Bonus;
import org.hamcrest.beans.SamePropertyValuesAs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {TestDataSourceConfig.class, MyBatisConfig.class, SnapshotJobLauncherTestUtils.class, BatchConfig.class})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:schema.sql", "classpath:/org/springframework/batch/core/schema-hsqldb.sql"})
public class BatchConfigTest {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDatasource(DataSource datasource) {
        jdbcTemplate = new JdbcTemplate(datasource);
    }


    @Autowired
    @Qualifier(value = "jobLauncherTestUtils")
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void testLaunchJob() throws Exception {

        Arrays.asList(
                "TRUNCATE TABLE BONUS;",
                "TRUNCATE TABLE EMP;",
                "TRUNCATE TABLE GRADE;",
                "INSERT INTO GRADE (GRADE_CODE, BONUS_MAGNIFICATION, FIXED_BONUS) VALUES (1, NULL, 150000);",
                "INSERT INTO GRADE (GRADE_CODE, BONUS_MAGNIFICATION, FIXED_BONUS) VALUES (2, 150, NULL);",
                "INSERT INTO GRADE (GRADE_CODE, BONUS_MAGNIFICATION, FIXED_BONUS) VALUES (3, 200, NULL);",
                "INSERT INTO GRADE (GRADE_CODE, BONUS_MAGNIFICATION, FIXED_BONUS) VALUES (4, 250, NULL);",
                "INSERT INTO GRADE (GRADE_CODE, BONUS_MAGNIFICATION, FIXED_BONUS) VALUES (5, 300, NULL);",
                "INSERT INTO EMP (EMP_ID, EMP_NAME, BASIC_SALARY, GRADE_CODE) VALUES (1, 'SCOTT', 200000, 1);",
                "INSERT INTO EMP (EMP_ID, EMP_NAME, BASIC_SALARY, GRADE_CODE) VALUES (2, 'ADAM', 250000, 2);",
                "INSERT INTO EMP (EMP_ID, EMP_NAME, BASIC_SALARY, GRADE_CODE) VALUES (3, 'MIKEs', 300000, 3);",
                "INSERT INTO EMP (EMP_ID, EMP_NAME, BASIC_SALARY, GRADE_CODE) VALUES (4, 'PHIL', 350000, 4);",
                "INSERT INTO EMP (EMP_ID, EMP_NAME, BASIC_SALARY, GRADE_CODE) VALUES (5, 'JACK', 400000, 5);")
        .forEach(s -> jdbcTemplate.execute(s));

        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus(), is(BatchStatus.COMPLETED));

        final List<Bonus> bonuses = jdbcTemplate.query(
                "SELECT * FROM BONUS",
                (ResultSet rs, int rowNum) -> {
                    return new Bonus(rs.getInt(1), rs.getInt(2));
                }
        );

        assertThat(bonuses.size(), is(5));

        assertThat(bonuses.get(0),
                SamePropertyValuesAs.samePropertyValuesAs(new Bonus(1, 150000)));
        assertThat(bonuses.get(1),
                SamePropertyValuesAs.samePropertyValuesAs(new Bonus(2, 375000)));
        assertThat(bonuses.get(2),
                SamePropertyValuesAs.samePropertyValuesAs(new Bonus(3, 600000)));
        assertThat(bonuses.get(3),
                SamePropertyValuesAs.samePropertyValuesAs(new Bonus(4, 875000)));
        assertThat(bonuses.get(4),
                SamePropertyValuesAs.samePropertyValuesAs(new Bonus(5, 1200000)));
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

@Configuration
class TestDataSourceConfig {

    @Bean
    public DataSource dataSource() throws ClassNotFoundException {

        return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .build();
    }
}
