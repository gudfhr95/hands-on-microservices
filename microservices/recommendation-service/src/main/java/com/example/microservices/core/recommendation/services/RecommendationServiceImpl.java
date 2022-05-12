package com.example.microservices.core.recommendation.services;

import com.example.api.core.recommendation.Recommendation;
import com.example.api.core.recommendation.RecommendationService;
import com.example.microservices.core.recommendation.persistence.RecommendationEntity;
import com.example.microservices.core.recommendation.persistence.RecommendationRepository;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

  private final RecommendationRepository repository;
  private final RecommendationMapper mapper;
  private final ServiceUtil serviceUtil;

  @Override
  public Recommendation createRecommendation(Recommendation body) {
    if (body.getProductId() < 1) {
      throw new InvalidInputException("Invalid productId: " + body.getProductId());
    }

    RecommendationEntity entity = mapper.apiToEntity(body);
    return repository.save(entity)
                     .log()
                     .onErrorMap(DuplicateKeyException.class, ex ->
                         new InvalidInputException(
                             "Duplicate key, Product Id: " + body.getProductId()
                                 + ", Recommendation Id: " + body.getRecommendationId()
                         )
                     )
                     .map(mapper::entityToApi)
                     .block();
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    return repository.findByProductId(productId)
                     .log()
                     .map(mapper::entityToApi)
                     .map(e -> {
                       e.setServiceAddress(serviceUtil.getServiceAddress());
                       return e;
                     });
  }

  @Override
  public void deleteRecommendations(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    log.debug(
        "deleteRecommendations: tries to delete recommendations for the product with productId: {}",
        productId
    );

    repository.deleteAll(repository.findByProductId(productId)).block();
  }
}
