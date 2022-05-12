package com.example.api.core.recommendation;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

public interface RecommendationService {

  @PostMapping(
      value = "/recommendation",
      consumes = "application/json",
      produces = "application/json"
  )
  Recommendation createRecommendation(@RequestBody Recommendation body);

  @GetMapping(
      value = "/recommendation",
      produces = "application/json"
  )
  Flux<Recommendation> getRecommendations(
      @RequestParam(value = "productId") int productId
  );

  @DeleteMapping(value = "/recommendation")
  void deleteRecommendations(@RequestParam(value = "productId") int productId);
}
