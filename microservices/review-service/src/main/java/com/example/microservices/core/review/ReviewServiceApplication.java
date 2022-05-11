package com.example.microservices.core.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.example")
@Slf4j
public class ReviewServiceApplication {

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(
        ReviewServiceApplication.class,
        args
    );

    String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
    log.info("Connected to MySQL: {}", mysqlUri);
  }
}
