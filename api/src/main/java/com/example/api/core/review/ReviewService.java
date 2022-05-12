package com.example.api.core.review;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

public interface ReviewService {

  @PostMapping(
      value = "/review",
      consumes = "application/json",
      produces = "application/json"
  )
  Review createReview(@RequestBody Review body);

  @GetMapping(
      value = "/review",
      produces = "application/json"
  )
  Flux<Review> getReviews(@RequestParam(value = "productId") int productId);

  @DeleteMapping(value = "/review")
  void deleteReviews(@RequestParam(value = "productId") int productId);
}
