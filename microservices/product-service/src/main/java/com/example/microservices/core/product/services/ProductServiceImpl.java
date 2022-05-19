package com.example.microservices.core.product.services;

import static reactor.core.publisher.Mono.error;

import com.example.api.core.product.Product;
import com.example.api.core.product.ProductService;
import com.example.microservices.core.product.persistence.ProductEntity;
import com.example.microservices.core.product.persistence.ProductRepository;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final ServiceUtil serviceUtil;
  private final ProductRepository repository;
  private final ProductMapper mapper;

  @Override
  public Product createProduct(Product body) {
    if (body.getProductId() < 1) {
      throw new InvalidInputException("Invalid productId: " + body.getProductId());
    }

    ProductEntity entity = mapper.apiToEntity(body);
    return repository.save(entity)
        .log()
        .onErrorMap(DuplicateKeyException.class, ex ->
            new InvalidInputException(
                "Duplicate key, Product Id: " + body.getProductId()
            ))
        .map(mapper::entityToApi)
        .block();
  }

  @Override
  public Mono<Product> getProduct(int productId, int delay, int faultPercent) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    if (delay > 0) {
      simulateDelay(delay);
    }

    if (faultPercent > 0) {
      throwErrorIfBadLuck(faultPercent);
    }

    return repository.findByProductId(productId)
        .log()
        .switchIfEmpty(error(
            new NotFoundException("No product found for productId: " + productId)
        ))
        .map(mapper::entityToApi)
        .map(e -> {
          e.setServiceAddress(serviceUtil.getServiceAddress());
          return e;
        });
  }

  @Override
  public void deleteProduct(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);

    repository.findByProductId(productId)
        .log()
        .map(repository::delete)
        .flatMap(e -> e)
        .block();
  }

  private void simulateDelay(int delay) {
    log.debug("Sleeping for {} seconds...", delay);

    try {
      Thread.sleep(delay * 1000);
    } catch (InterruptedException e) {
    }

    log.debug("Moving on...");
  }

  private void throwErrorIfBadLuck(int faultPercent) {
    int randomThreshold = getRandomNumber(1, 1000);

    if (faultPercent < randomThreshold) {
      log.debug("We got lucky, no error occurred, {} < {}", faultPercent, randomThreshold);
    } else {
      log.debug("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold);
      throw new RuntimeException("Something went wrong...");
    }
  }

  private final Random randomNumberGenerator = new Random();

  private int getRandomNumber(int min, int max) {
    if (max < min) {
      throw new RuntimeException("Max must be greater than min");
    }

    return randomNumberGenerator.nextInt((max - min) + 1) + min;
  }
}
