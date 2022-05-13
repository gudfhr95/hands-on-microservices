package com.example.microservices.core.review.services;

import com.example.api.core.review.Review;
import com.example.api.core.review.ReviewService;
import com.example.api.event.Event;
import com.example.util.exceptions.EventProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
@RequiredArgsConstructor
@Slf4j
public class MessageProcessor {

  private final ReviewService reviewService;

  @StreamListener(target = Sink.INPUT)
  public void process(Event<Integer, Review> event) {
    log.info("Process message created at {}...", event.getEventCreatedAt());

    switch (event.getEventType()) {
      case CREATE:
        Review review = event.getData();
        log.info("Create review with ID: {}/{}", review.getProductId(),
            review.getReviewId());
        reviewService.createReview(review);
        break;

      case DELETE:
        int productId = event.getKey();
        log.info("Delete reviews with ProductID: {}", productId);
        reviewService.deleteReviews(productId);
        break;

      default:
        String errorMessage =
            "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
        log.warn(errorMessage);
        throw new EventProcessingException(errorMessage);
    }

    log.info("Message processing done!");
  }
}
