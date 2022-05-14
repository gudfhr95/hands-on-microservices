package com.example.microservices.composite.product.actuator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.NamedContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component("CoreServices")
public class CoreServiceCompositeReactiveHealthContributor implements
    CompositeReactiveHealthContributor {

  private final Map<String, ReactiveHealthContributor> contributors = new LinkedHashMap<>();

  public CoreServiceCompositeReactiveHealthContributor(
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") int productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") int recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") int reviewServicePort
  ) {
    WebClient webClient = WebClient.builder().build();

    contributors.put("product", new CoreServiceHealthContributor(
        getActuatorUrl(productServiceHost, productServicePort), webClient));
    contributors.put("recommendation", new CoreServiceHealthContributor(
        getActuatorUrl(recommendationServiceHost, recommendationServicePort), webClient));
    contributors.put("review", new CoreServiceHealthContributor(
        getActuatorUrl(reviewServiceHost, reviewServicePort), webClient));
  }
  
  @Override
  public ReactiveHealthContributor getContributor(String name) {
    return contributors.get(name);
  }

  @Override
  public Iterator<NamedContributor<ReactiveHealthContributor>> iterator() {
    return contributors.entrySet()
        .stream()
        .map((entry) -> NamedContributor.of(entry.getKey(), entry.getValue()))
        .iterator();
  }

  private String getActuatorUrl(String host, int port) {
    return "http://" + host + ":" + port + "/actuator/health";
  }
}
