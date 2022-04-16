package com.example.microservices.composite.product.services;

import static org.springframework.http.HttpMethod.GET;

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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService,
    ReviewService {

  private final RestTemplate restTemplate;
  private final ObjectMapper mapper;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  @Autowired
  public ProductCompositeIntegration(
      RestTemplate restTemplate,
      ObjectMapper mapper,
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") int productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") int recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") int reviewServicePort
  ) {
    this.restTemplate = restTemplate;
    this.mapper = mapper;

    this.productServiceUrl =
        "http://" + productServiceHost + ":" + productServicePort + "/product/";
    this.recommendationServiceUrl =
        "http://" + recommendationServiceHost + ":" + recommendationServicePort
            + "/recommendation?productId=";
    this.reviewServiceUrl =
        "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
  }

  public Product getProduct(int productId) {
    try {
      String url = productServiceUrl + productId;

      Product product = restTemplate.getForObject(url, Product.class);

      return product;
    } catch (HttpClientErrorException ex) {
      switch (ex.getStatusCode()) {
        case NOT_FOUND:
          throw new NotFoundException(getErrorMessage(ex));
        case UNPROCESSABLE_ENTITY:
          throw new InvalidInputException(getErrorMessage(ex));
        default:
          throw ex;
      }
    }
  }

  private String getErrorMessage(HttpClientErrorException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }

  public List<Recommendation> getRecommendations(int productId) {
    String url = recommendationServiceUrl + productId;
    List<Recommendation> recommendations = restTemplate.exchange(
        url,
        GET,
        null,
        new ParameterizedTypeReference<List<Recommendation>>() {
        }
    ).getBody();
    return recommendations;
  }

  public List<Review> getReviews(int productId) {
    String url = reviewServiceUrl + productId;
    List<Review> reviews = restTemplate.exchange(
        url,
        GET,
        null,
        new ParameterizedTypeReference<List<Review>>() {
        }
    ).getBody();
    return reviews;
  }
}
