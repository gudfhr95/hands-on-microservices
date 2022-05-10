package com.example.microservices.core.product.services;

import com.example.api.core.product.Product;
import com.example.api.core.product.ProductService;
import com.example.microservices.core.product.persistence.ProductEntity;
import com.example.microservices.core.product.persistence.ProductRepository;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final ServiceUtil serviceUtil;
  private final ProductRepository repository;
  private final ProductMapper mapper;

  @Override
  public Product createProduct(Product body) {
    try {
      ProductEntity entity = mapper.apiToEntity(body);
      ProductEntity newEntity = repository.save(entity);

      log.debug("createProduct: entity created for productId: {}", body.getProductId());

      return mapper.entityToApi(newEntity);
    } catch (DuplicateKeyException e) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
    }
  }

  @Override
  public Product getProduct(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    ProductEntity entity = repository.findByProductId(productId)
                                     .orElseThrow(() -> new NotFoundException(
                                         "No product found for productId: " + productId));

    Product response = mapper.entityToApi(entity);
    response.setServiceAddress(serviceUtil.getServiceAddress());

    log.debug("getProduct: found productId: {}", response.getProductId());

    return response;
  }

  @Override
  public void deleteProduct(int productId) {
    log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);

    repository.findByProductId(productId).ifPresent(repository::delete);
  }
}
