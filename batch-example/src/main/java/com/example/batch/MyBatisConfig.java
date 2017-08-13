package com.example.batch;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;


@Configuration
@MapperScan("com.example.batch.repository")
public class MyBatisConfig {

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean() throws ClassNotFoundException {
        final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(datasource());
        return sqlSessionFactoryBean;
    }

    @Bean
    public DataSource datasource() throws ClassNotFoundException {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass((Class<? extends java.sql.Driver>) Class.forName("org.postgresql.Driver"));
        dataSource.setUrl("jdbc:postgresql://localhost:5432/");
        dataSource.setUsername("scott");
        dataSource.setPassword("tiger");
        return dataSource;

    }

}
