package com.example.microservices.core.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;
import static org.springframework.data.domain.Sort.Direction.ASC;

import com.example.microservices.core.product.persistence.ProductEntity;
import com.example.microservices.core.product.persistence.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataMongoTest
class PersistenceTests {

  @Autowired
  private ProductRepository repository;

  private ProductEntity savedEntity;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();

    ProductEntity entity = new ProductEntity(1, "n", 1);
    savedEntity = repository.save(entity);

    assertThat(savedEntity).isEqualTo(entity);
  }

  @Test
  void create() {
    ProductEntity newEntity = new ProductEntity(2, "n", 2);
    repository.save(newEntity);

    ProductEntity foundEntity = repository.findById(newEntity.getId()).get();

    assertThat(foundEntity).isEqualTo(newEntity);
    assertThat(repository.count()).isEqualTo(2);
  }

  @Test
  void update() {
    savedEntity.setName("n2");
    repository.save(savedEntity);

    ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();

    assertThat(foundEntity.getVersion()).isOne();
    assertThat(foundEntity.getName()).isEqualTo("n2");
  }

  @Test
  void delete() {
    repository.delete(savedEntity);
    assertThat(repository.existsById(savedEntity.getId())).isFalse();
  }

  @Test
  void getProductId() {
    Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());

    assertThat(entity.isPresent()).isTrue();
    assertThat(entity.get()).isEqualTo(savedEntity);
  }

  @Test
  void duplicateError() {
    ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);

    assertThatThrownBy(() -> repository.save(entity))
        .isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void optimisticLockError() {
    // Store the saved entity in two separate entity objects
    ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
    ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

    // Update the entity using the first entity object
    entity1.setName("n1");
    repository.save(entity1);

    // Update the entity using the second entity object.
    // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
    try {
      entity2.setName("n2");
      repository.save(entity2);

      fail("Expected an OptimisticLockingFailureException");
    } catch (OptimisticLockingFailureException e) {
    }

    // Get the updated entity from the database and verify its new state
    ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();

    assertThat(updatedEntity.getVersion()).isOne();
    assertThat(updatedEntity.getName()).isEqualTo("n1");
  }

  @Test
  void paging() {
    repository.deleteAll();

    List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010)
                                               .mapToObj(i -> new ProductEntity(i, "name " + i, i))
                                               .collect(Collectors.toList());
    repository.saveAll(newProducts);

    Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
    nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
    nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
    nextPage = testNextPage(nextPage, "[1009, 1010]", false);
  }

  private Pageable testNextPage(
      Pageable nextPage,
      String expectedProductIds,
      boolean expectsNextPage
  ) {
    Page<ProductEntity> productPage = repository.findAll(nextPage);

    assertThat(productPage.getContent()
                          .stream()
                          .map(p -> p.getProductId())
                          .collect(Collectors.toList())
                          .toString()).isEqualTo(expectedProductIds);
    assertThat(productPage.hasNext()).isEqualTo(expectsNextPage);

    return productPage.nextPageable();
  }
}
