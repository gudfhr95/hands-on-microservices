package com.example.microservices.core.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import com.example.microservices.core.review.persistence.ReviewEntity;
import com.example.microservices.core.review.persistence.ReviewRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
class PersistenceTests {

  @Autowired
  private ReviewRepository repository;

  private ReviewEntity savedEntity;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();

    ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
    savedEntity = repository.save(entity);

    assertThat(savedEntity).isEqualTo(entity);
  }

  @Test
  void create() {
    ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", "c");
    repository.save(newEntity);

    ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
    assertThat(foundEntity).isEqualTo(newEntity);

    assertThat(repository.count()).isEqualTo(2);
  }

  @Test
  void update() {
    savedEntity.setAuthor("a2");
    repository.save(savedEntity);

    ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();

    assertThat(foundEntity.getVersion()).isOne();
    assertThat(foundEntity.getAuthor()).isEqualTo("a2");
  }

  @Test
  void delete() {
    repository.delete(savedEntity);

    assertThat(repository.existsById(savedEntity.getId())).isFalse();
  }

  @Test
  void getByProductId() {
    List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

    assertThat(entityList.size()).isOne();
    assertThat(entityList.get(0)).isEqualTo(savedEntity);
  }

  @Test
  void duplicationError() {
    ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");

    assertThatThrownBy(() -> {
      repository.save(entity);
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void optimisticLockError() {
    // Store the saved entity in two separate entity objects
    ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
    ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

    // Update the entity using the first entity object
    entity1.setAuthor("a1");
    repository.save(entity1);

    // Update the entity using the second entity object.
    // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
    try {
      entity2.setAuthor("a2");
      repository.save(entity2);

      fail("Expected an OptimisticLockingFailureException");
    } catch (OptimisticLockingFailureException e) {
    }

    // Get the updated entity from the database and verify its new state
    ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();

    assertThat(updatedEntity.getVersion()).isOne();
    assertThat(updatedEntity.getAuthor()).isEqualTo("a1");
  }
}
