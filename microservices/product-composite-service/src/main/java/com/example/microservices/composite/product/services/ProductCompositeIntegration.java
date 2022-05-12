package com.example.microservices.composite.product.services;

import static reactor.core.publisher.Flux.empty;

import com.example.api.core.product.Product;
import com.example.api.core.product.ProductService;
import com.example.api.core.recommendation.Recommendation;
import com.example.api.core.recommendation.RecommendationService;
import com.example.api.core.review.Review;
import com.example.api.core.review.ReviewService;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService,
    ReviewService {

  private final WebClient webClient;
  private final ObjectMapper mapper;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  public ProductCompositeIntegration(
      WebClient.Builder webClient,
      ObjectMapper mapper,
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") int productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") int recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") int reviewServicePort
  ) {
    this.webClient = webClient.build();
    this.mapper = mapper;

    this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort;
    this.recommendationServiceUrl =
        "http://" + recommendationServiceHost + ":" + recommendationServicePort;
    this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort;
  }

  @Override
  public Product createProduct(Product body) {
    try {
      String url = productServiceUrl;
      log.debug("Will post a new product to URL: {}", url);

      Product product = restTemplate.postForObject(url, body, Product.class);
      log.debug("Created a product with id: {}", product.getProductId());

      return product;
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Mono<Product> getProduct(int productId) {
    String url = productServiceUrl + "/product/" + productId;

    log.debug("Will call the getProduct API on URL: {}", url);

    return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Product.class)
                    .log()
                    .onErrorMap(WebClientResponseException.class, this::handleException);
  }

  @Override
  public void deleteProduct(int productId) {
    try {
      String url = productServiceUrl + "/" + productId;
      log.debug("Will call the deleteProduct API on URL: {}", url);

      restTemplate.delete(url);
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Recommendation createRecommendation(Recommendation body) {
    try {
      String url = recommendationServiceUrl;
      log.debug("Will post a new recommendation to URL: {}", url);

      Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
      log.debug("Created a recommendation with id: {}", recommendation.getProductId());

      return recommendation;
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    String url = recommendationServiceUrl + "/recommendation?productId=" + productId;

    log.debug("Will call the getRecommendations API on URL: {}", url);

    // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
    return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToFlux(Recommendation.class)
                    .log()
                    .onErrorResume(error -> empty());
  }

  @Override
  public void deleteRecommendations(int productId) {
    try {
      String url = recommendationServiceUrl + "?productId=" + productId;
      log.debug("Will call the deleteRecommendation API on URL: {}", url);

      restTemplate.delete(url);
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Review createReview(Review body) {
    try {
      String url = reviewServiceUrl;
      log.debug("Will post a new review to URL: {}", url);

      Review review = restTemplate.postForObject(url, body, Review.class);
      log.debug("Created a review with id: {}", review.getProductId());

      return review;
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Flux<Review> getReviews(int productId) {
    String url = reviewServiceUrl + "/review?productId=" + productId;

    log.debug("Will call the getReviews API on URL: {}", url);

    // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
    return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToFlux(Review.class)
                    .log()
                    .onErrorResume(error -> empty());
  }

  @Override
  public void deleteReviews(int productId) {
    try {
      String url = reviewServiceUrl + "?productId=" + productId;
      log.debug("Will call the deleteReviews API on URL: {}", url);

      restTemplate.delete(url);
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  private Throwable handleException(Throwable ex) {
    if (!(ex instanceof WebClientResponseException)) {
      log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
      return ex;
    }

    WebClientResponseException wcre = (WebClientResponseException) ex;

    switch (wcre.getStatusCode()) {
      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(wcre));

      case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(wcre));

      default:
        log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.toString());
        log.warn("Error Body: {}", wcre.getResponseBodyAsString());
        return ex;
    }
  }

  private String getErrorMessage(WebClientResponseException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }
}
