package com.allanweber.candidatesprocesses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SuppressWarnings("PMD")
@EnableConfigurationProperties
@SpringBootApplication(exclude={MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class CandidatesProcessesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CandidatesProcessesApplication.class, args);
	}

}
