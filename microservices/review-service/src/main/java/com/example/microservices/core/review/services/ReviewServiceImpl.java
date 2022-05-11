package com.example.microservices.core.review.services;

import com.example.api.core.review.Review;
import com.example.api.core.review.ReviewService;
import com.example.microservices.core.review.persistence.ReviewEntity;
import com.example.microservices.core.review.persistence.ReviewRepository;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository repository;
  private final ReviewMapper mapper;
  private final ServiceUtil serviceUtil;

  @Override
  public Review createReview(Review body) {
    try {
      ReviewEntity entity = mapper.apiToEntity(body);
      ReviewEntity newEntity = repository.save(entity);

      log.debug(
          "createReview: created a review entity: {}/{}",
          body.getProductId(),
          body.getReviewId()
      );

      return mapper.entityToApi(newEntity);
    } catch (DataIntegrityViolationException e) {
      throw new InvalidInputException(
          "Duplicate key, Product Id: " + body.getProductId() + ", Review Id: "
              + body.getReviewId());
    }
  }

  @Override
  public List<Review> getReviews(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    List<ReviewEntity> entityList = repository.findByProductId(productId);
    List<Review> list = mapper.entityListToApiList(entityList);
    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    log.debug("getReviews: response size: {}", list.size());

    return list;
  }

  @Override
  public void deleteReviews(int productId) {
    log.debug(
        "deleteReviews: tries to delete reviews for the product with productId: {}",
        productId
    );

    repository.deleteAll(repository.findByProductId(productId));
  }
}
