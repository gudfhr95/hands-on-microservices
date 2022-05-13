package com.example.microservices.composite.product.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class CoreServiceHealthContributor implements ReactiveHealthIndicator,
    ReactiveHealthContributor {

  private final String url;
  private final WebClient webClient;

  @Override
  public Mono<Health> health() {
    log.debug("Will call the Health API on URL: {}", url);

    return webClient.get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .map(s -> new Health.Builder().up().build())
        .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
        .log();
  }
}
