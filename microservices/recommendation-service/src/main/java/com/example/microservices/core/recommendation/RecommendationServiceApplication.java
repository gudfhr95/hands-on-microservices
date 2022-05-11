package com.example.microservices.core.recommendation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.example")
@Slf4j
public class RecommendationServiceApplication {

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(
        RecommendationServiceApplication.class,
        args
    );

    String mongoDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
    String mongoDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
    log.info("Connected to MongoDb: {}:{}", mongoDbHost, mongoDbPort);
  }
}
