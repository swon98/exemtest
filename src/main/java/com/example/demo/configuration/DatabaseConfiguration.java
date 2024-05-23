package com.example.demo.configuration;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


@Configuration
@PropertySource("classpath:/application.yaml")
public class DatabaseConfiguration {
	
	@Autowired
	private ApplicationContext applicationContext;
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.hikari") 
	public HikariConfig hikariConfig() {
	return new HikariConfig();
	}
	@Bean
	@ConfigurationProperties(prefix = "mybatis.configuration")	// 마이바티스 설정을 가져옴
	public org.apache.ibatis.session.Configuration mybatisConfig() {
	return new org.apache.ibatis.session.Configuration();
	}
	@Bean
	public DataSource dataSource() {
	DataSource dataSource = new HikariDataSource(hikariConfig());
	return dataSource;
	}
	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
	SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
	sqlSessionFactoryBean.setDataSource(dataSource);
	sqlSessionFactoryBean.setMapperLocations(
	applicationContext.getResources("classpath:/mapper/*.xml"));	//resources 하위 경로
	sqlSessionFactoryBean.setConfiguration(mybatisConfig());
	sqlSessionFactoryBean.setTypeAliasesPackage("com.example.demo.model");
	return sqlSessionFactoryBean.getObject();
	}

}
