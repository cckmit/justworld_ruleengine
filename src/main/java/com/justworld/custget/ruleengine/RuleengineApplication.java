package com.justworld.custget.ruleengine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@MapperScan("com.justworld.custget.ruleengine.dao")
public class RuleengineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RuleengineApplication.class, args);
	}
}
