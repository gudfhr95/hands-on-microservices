package com.example.microservices.core.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

import com.example.microservices.core.recommendation.persistence.RecommendationEntity;
import com.example.microservices.core.recommendation.persistence.RecommendationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

@DataMongoTest
class PersistenceTests {

  @Autowired
  private RecommendationRepository repository;

  private RecommendationEntity savedEntity;

  @BeforeEach
  void setupDb() {
    repository.deleteAll().block();

    RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
    savedEntity = repository.save(entity).block();

    assertThat(savedEntity).isEqualTo(entity);
  }

  @Test
  void create() {
    RecommendationEntity newEntity = new RecommendationEntity(1, 3, "a", 3, "c");
    repository.save(newEntity).block();

    RecommendationEntity foundEntity = repository.findById(newEntity.getId()).block();

    assertThat(foundEntity).isEqualTo(newEntity);
    assertThat(repository.count().block()).isEqualTo(2);
  }

  @Test
  void update() {
    savedEntity.setAuthor("a2");
    repository.save(savedEntity).block();

    RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();

    assertThat(foundEntity.getVersion()).isOne();
    assertThat(foundEntity.getAuthor()).isEqualTo("a2");
  }

  @Test
  void delete() {
    repository.delete(savedEntity).block();

    assertThat(repository.existsById(savedEntity.getId()).block()).isFalse();
  }

  @Test
  void getByProductId() {
    List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId())
                                                      .collectList()
                                                      .block();

    assertThat(entityList.size()).isOne();
    assertThat(entityList.get(0)).isEqualTo(savedEntity);
  }

  @Test
  void duplicationError() {
    RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");

    assertThatThrownBy(() -> {
      repository.save(entity).block();
    }).isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void optimisticLockError() {
    // Store the saved entity in two separate entity objects
    RecommendationEntity entity1 = repository.findById(savedEntity.getId()).block();
    RecommendationEntity entity2 = repository.findById(savedEntity.getId()).block();

    // Update the entity using the first entity object
    entity1.setAuthor("a1");
    repository.save(entity1).block();

    // Update the entity using the second entity object.
    // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
    try {
      entity2.setAuthor("a2");
      repository.save(entity2).block();

      fail("Expected an OptimisticLockingFailureException");
    } catch (OptimisticLockingFailureException e) {
    }

    // Get the updated entity from the database and verify its new state
    RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).block();

    assertThat(updatedEntity.getVersion()).isOne();
    assertThat(updatedEntity.getAuthor()).isEqualTo("a1");
  }
}
