package com.example.microservices.core.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import com.example.api.core.review.Review;
import com.example.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTests {

  @Autowired
  private WebTestClient client;

  @Autowired
  private ReviewRepository repository;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();
  }

  @Test
  void getReviewByProductId() {
    int productId = 1;

    postAndVerifyReview(productId, 1, OK);
    postAndVerifyReview(productId, 2, OK);
    postAndVerifyReview(productId, 3, OK);

    assertThat(repository.findByProductId(productId).size()).isEqualTo(3);

    getAndVerifyReviewsByProductId(productId, OK)
        .jsonPath("$.length()").isEqualTo(3)
        .jsonPath("$[2].productId").isEqualTo(productId)
        .jsonPath("$[2].reviewId").isEqualTo(3);
  }

  @Test
  void duplicateError() {
    int productId = 1;
    int reviewId = 1;

    postAndVerifyReview(productId, reviewId, OK)
        .jsonPath("$.productId").isEqualTo(productId)
        .jsonPath("$.reviewId").isEqualTo(reviewId);

    assertThat(repository.count()).isOne();

    postAndVerifyReview(productId, reviewId, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id: 1");

    assertThat(repository.count()).isOne();
  }

  @Test
  void deleteReviews() {
    int productId = 1;
    int reviewId = 1;

    postAndVerifyReview(productId, reviewId, OK);
    assertThat(repository.findByProductId(productId).size()).isOne();

    deleteAndVerifyReviewsByProductId(productId, OK);
    assertThat(repository.findByProductId(productId).size()).isZero();

    deleteAndVerifyReviewsByProductId(productId, OK);
  }

  @Test
  void getReviewsMissingParameter() {
    getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getReviewsNotFound() {
    getAndVerifyReviewsByProductId(213, OK)
        .jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void getReviewsInvalidParameterNegativeValue() {
    int productIdInvalid = -1;

    getAndVerifyReviewsByProductId(productIdInvalid, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
  }

  private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(
      int productId,
      HttpStatus expectedStatus
  ) {
    return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(
      String productIdQuery,
      HttpStatus expectedStatus
  ) {
    return client.get()
                 .uri("/review" + productIdQuery)
                 .accept(APPLICATION_JSON)
                 .exchange()
                 .expectStatus().isEqualTo(expectedStatus)
                 .expectHeader().contentType(APPLICATION_JSON)
                 .expectBody();
  }

  private WebTestClient.BodyContentSpec postAndVerifyReview(
      int productId, int reviewId,
      HttpStatus expectedStatus
  ) {
    Review review = new Review(
        productId,
        reviewId,
        "Author " + reviewId,
        "Subject " + reviewId,
        "Content " + reviewId,
        "SA"
    );

    return client.post()
                 .uri("/review")
                 .body(just(review), Review.class)
                 .accept(APPLICATION_JSON)
                 .exchange()
                 .expectStatus().isEqualTo(expectedStatus)
                 .expectHeader().contentType(APPLICATION_JSON)
                 .expectBody();
  }

  private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(
      int productId,
      HttpStatus expectedStatus
  ) {
    return client.delete()
                 .uri("/review?productId=" + productId)
                 .accept(APPLICATION_JSON)
                 .exchange()
                 .expectStatus().isEqualTo(expectedStatus)
                 .expectBody();
  }
}
