package com.example.microservices.core.recommendation.services;

import com.example.api.core.recommendation.Recommendation;
import com.example.api.core.recommendation.RecommendationService;
import com.example.microservices.core.recommendation.persistence.RecommendationEntity;
import com.example.microservices.core.recommendation.persistence.RecommendationRepository;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

  private final RecommendationRepository repository;
  private final RecommendationMapper mapper;
  private final ServiceUtil serviceUtil;

  @Override
  public Recommendation createRecommendation(Recommendation body) {
    try {
      RecommendationEntity entity = mapper.apiToEntity(body);
      RecommendationEntity newEntity = repository.save(entity);

      log.debug(
          "createRecommendation: create a recommendation entity: {}/{}",
          body.getProductId(),
          body.getRecommendationId()
      );

      return mapper.entityToApi(newEntity);
    } catch (DuplicateKeyException e) {
      throw new InvalidInputException(
          "Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id: "
              + body.getRecommendationId());
    }
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    List<RecommendationEntity> entityList = repository.findByProductId(productId);
    List<Recommendation> list = mapper.entityListToApiList(entityList);
    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    log.debug("getRecommendations: response size: {}", list.size());

    return list;
  }

  @Override
  public void deleteRecommendations(int productId) {
    log.debug(
        "deleteRecommendations: tries to delete recommendations for the product with productId: {}",
        productId
    );

    repository.deleteAll(repository.findByProductId(productId));
  }
}
