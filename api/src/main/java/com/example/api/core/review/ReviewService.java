package com.example.api.core.review;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

public interface ReviewService {

  Review createReview(@RequestBody Review body);

  @GetMapping(
      value = "/review",
      produces = "application/json"
  )
  Flux<Review> getReviews(@RequestParam(value = "productId") int productId);

  void deleteReviews(@RequestParam(value = "productId") int productId);
}
