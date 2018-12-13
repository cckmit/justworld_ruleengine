package com.justworld.custget.ruleengine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableTransactionManagement
@SpringBootApplication
@EnableScheduling
@MapperScan("com.justworld.custget.ruleengine.dao")
@ComponentScan("com.justworld.custget")
public class RuleengineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RuleengineApplication.class, args);
	}
}
