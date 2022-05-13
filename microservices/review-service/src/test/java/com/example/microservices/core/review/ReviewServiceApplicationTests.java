package com.example.microservices.core.review;

import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.example.api.core.review.Review;
import com.example.api.event.Event;
import com.example.microservices.core.review.persistence.ReviewRepository;
import com.example.util.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTests {

  @Autowired
  private WebTestClient client;

  @Autowired
  private ReviewRepository repository;

  @Autowired
  private Sink channels;

  private AbstractMessageChannel input = null;

  @BeforeEach
  void setupDb() {
    input = (AbstractMessageChannel) channels.input();
    repository.deleteAll();
  }

  @Test
  void getReviewByProductId() {
    int productId = 1;

    assertThat(repository.findByProductId(productId).size()).isZero();

    sendCreateReviewEvent(productId, 1);
    sendCreateReviewEvent(productId, 2);
    sendCreateReviewEvent(productId, 3);

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

    assertThat(repository.count()).isZero();

    sendCreateReviewEvent(productId, reviewId);

    assertThat(repository.count()).isOne();

    try {
      sendCreateReviewEvent(productId, reviewId);
      fail("Expected a MessagingException here!");
    } catch (MessagingException me) {
      if (me.getCause() instanceof InvalidInputException) {
        InvalidInputException iie = (InvalidInputException) me.getCause();
        assertThat(iie.getMessage()).isEqualTo(
            "Duplicate key, Product Id: " + productId + ", Review Id: " + reviewId);
      } else {
        fail("Expected a InvalidInputException as the root cause!");
      }
    }

    assertThat(repository.count()).isOne();
  }

  @Test
  void deleteReviews() {
    int productId = 1;
    int reviewId = 1;

    sendCreateReviewEvent(productId, reviewId);
    assertThat(repository.findByProductId(productId).size()).isOne();

    sendDeleteReviewEvent(productId);
    assertThat(repository.findByProductId(productId).size()).isZero();

    sendDeleteReviewEvent(productId);
  }

  @Test
  void getReviewsMissingParameter() {
    getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getReviewsNotFound() {
    getAndVerifyReviewsByProductId(113, OK)
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

  private void sendCreateReviewEvent(int productId, int reviewId) {
    Review review = new Review(productId, reviewId,
        "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
    Event<Integer, Review> event = new Event<>(CREATE, productId, review);
    input.send(new GenericMessage<>(event));
  }

  private void sendDeleteReviewEvent(int productId) {
    Event<Integer, Review> event = new Event<>(DELETE, productId, null);
    input.send(new GenericMessage<>(event));
  }
}
