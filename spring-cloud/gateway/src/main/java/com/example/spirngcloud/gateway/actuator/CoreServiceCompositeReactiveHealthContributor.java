package com.example.spirngcloud.gateway.actuator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.NamedContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component("CoreServices")
public class CoreServiceCompositeReactiveHealthContributor implements
    CompositeReactiveHealthContributor {

  private final String authServerUrl = "http://auth-server";
  private final String productServiceUrl = "http://product";
  private final String recommendationServiceUrl = "http://recommendation";
  private final String reviewServiceUrl = "http://review";
  private final String productCompositeServiceUrl = "http://product-composite";

  private final Map<String, ReactiveHealthContributor> contributors = new LinkedHashMap<>();

  public CoreServiceCompositeReactiveHealthContributor(WebClient.Builder webClientBuilder) {
    WebClient webClient = webClientBuilder.build();

    contributors.put("auth-server",
        new CoreServiceHealthContributor(getActuatorUrl(authServerUrl), webClient));
    contributors.put("product",
        new CoreServiceHealthContributor(getActuatorUrl(productServiceUrl), webClient));
    contributors.put("recommendation",
        new CoreServiceHealthContributor(getActuatorUrl(recommendationServiceUrl), webClient));
    contributors.put("review",
        new CoreServiceHealthContributor(getActuatorUrl(reviewServiceUrl), webClient));
    contributors.put("product-composite",
        new CoreServiceHealthContributor(getActuatorUrl(productCompositeServiceUrl), webClient));
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

  private String getActuatorUrl(String host) {
    return host + "/actuator/health";
  }
}
