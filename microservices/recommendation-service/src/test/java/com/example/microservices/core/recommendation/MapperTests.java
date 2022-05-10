package com.example.microservices.core.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.api.core.recommendation.Recommendation;
import com.example.microservices.core.recommendation.persistence.RecommendationEntity;
import com.example.microservices.core.recommendation.services.RecommendationMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MapperTests {

  private RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

  @Test
  void mapperTests() {
    assertThat(mapper).isNotNull();

    Recommendation api = new Recommendation(1, 2, "a", 4, "c", "adr");

    RecommendationEntity entity = mapper.apiToEntity(api);

    assertThat(entity.getProductId()).isEqualTo(api.getProductId());
    assertThat(entity.getRecommendationId()).isEqualTo(api.getRecommendationId());
    assertThat(entity.getAuthor()).isEqualTo(api.getAuthor());
    assertThat(entity.getRating()).isEqualTo(api.getRate());
    assertThat(entity.getContent()).isEqualTo(api.getContent());

    Recommendation api2 = mapper.entityToApi(entity);

    assertThat(api2.getProductId()).isEqualTo(api.getProductId());
    assertThat(api2.getRecommendationId()).isEqualTo(api.getRecommendationId());
    assertThat(api2.getAuthor()).isEqualTo(api.getAuthor());
    assertThat(api2.getRate()).isEqualTo(api.getRate());
    assertThat(api2.getContent()).isEqualTo(api.getContent());
    assertThat(api2.getServiceAddress()).isNull();
  }

  @Test
  void mapperListTests() {
    assertThat(mapper).isNotNull();

    Recommendation api = new Recommendation(1, 2, "a", 4, "c", "adr");
    List<Recommendation> apiList = Collections.singletonList(api);

    List<RecommendationEntity> entityList = mapper.apiListToEntityList(apiList);

    assertThat(entityList.size()).isEqualTo(apiList.size());

    RecommendationEntity entity = entityList.get(0);

    assertThat(entity.getProductId()).isEqualTo(api.getProductId());
    assertThat(entity.getRecommendationId()).isEqualTo(api.getRecommendationId());
    assertThat(entity.getAuthor()).isEqualTo(api.getAuthor());
    assertThat(entity.getRating()).isEqualTo(api.getRate());
    assertThat(entity.getContent()).isEqualTo(api.getContent());

    List<Recommendation> api2List = mapper.entityListToApiList(entityList);

    assertThat(api2List.size()).isEqualTo(apiList.size());

    Recommendation api2 = api2List.get(0);

    assertThat(api2.getProductId()).isEqualTo(api.getProductId());
    assertThat(api2.getRecommendationId()).isEqualTo(api.getRecommendationId());
    assertThat(api2.getAuthor()).isEqualTo(api.getAuthor());
    assertThat(api2.getRate()).isEqualTo(api.getRate());
    assertThat(api2.getContent()).isEqualTo(api.getContent());
    assertThat(api2.getServiceAddress()).isNull();
  }
}
