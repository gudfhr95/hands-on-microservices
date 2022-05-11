package com.example.microservices.core.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.api.core.review.Review;
import com.example.microservices.core.review.persistence.ReviewEntity;
import com.example.microservices.core.review.services.ReviewMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MapperTests {

  private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

  @Test
  void mapperTests() {
    assertThat(mapper).isNotNull();

    Review api = new Review(1, 2, "a", "s", "c", "adr");

    ReviewEntity entity = mapper.apiToEntity(api);

    assertThat(entity.getProductId()).isEqualTo(api.getProductId());
    assertThat(entity.getReviewId()).isEqualTo(api.getReviewId());
    assertThat(entity.getAuthor()).isEqualTo(api.getAuthor());
    assertThat(entity.getSubject()).isEqualTo(api.getSubject());
    assertThat(entity.getContent()).isEqualTo(api.getContent());

    Review api2 = mapper.entityToApi(entity);

    assertThat(api2.getProductId()).isEqualTo(api.getProductId());
    assertThat(api2.getReviewId()).isEqualTo(api.getReviewId());
    assertThat(api2.getAuthor()).isEqualTo(api.getAuthor());
    assertThat(api2.getSubject()).isEqualTo(api.getSubject());
    assertThat(api2.getContent()).isEqualTo(api.getContent());
    assertThat(api2.getServiceAddress()).isNull();
  }

  @Test
  void mapperListTests() {
    assertThat(mapper).isNotNull();

    Review api = new Review(1, 2, "a", "s", "c", "adr");
    List<Review> apiList = Collections.singletonList(api);

    List<ReviewEntity> entityList = mapper.apiListToEntityList(apiList);

    assertThat(entityList.size()).isEqualTo(apiList.size());

    ReviewEntity entity = entityList.get(0);

    assertThat(entity.getProductId()).isEqualTo(api.getProductId());
    assertThat(entity.getReviewId()).isEqualTo(api.getReviewId());
    assertThat(entity.getAuthor()).isEqualTo(api.getAuthor());
    assertThat(entity.getSubject()).isEqualTo(api.getSubject());
    assertThat(entity.getContent()).isEqualTo(api.getContent());

    List<Review> api2List = mapper.entityListToApiList(entityList);

    assertThat(api2List.size()).isEqualTo(apiList.size());

    Review api2 = api2List.get(0);

    assertThat(api2.getProductId()).isEqualTo(api.getProductId());
    assertThat(api2.getReviewId()).isEqualTo(api.getReviewId());
    assertThat(api2.getAuthor()).isEqualTo(api.getAuthor());
    assertThat(api2.getSubject()).isEqualTo(api.getSubject());
    assertThat(api2.getContent()).isEqualTo(api.getContent());
    assertThat(api2.getServiceAddress()).isNull();
  }
}
