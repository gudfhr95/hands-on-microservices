package com.example.microservices.core.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.api.core.product.Product;
import com.example.microservices.core.product.persistence.ProductEntity;
import com.example.microservices.core.product.services.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MapperTests {

  private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

  @Test
  void mapperTests() {
    assertThat(mapper).isNotNull();

    Product api = new Product(1, "n", 1, "sa");

    ProductEntity entity = mapper.apiToEntity(api);

    assertThat(entity.getProductId()).isEqualTo(api.getProductId());
    assertThat(entity.getName()).isEqualTo(api.getName());
    assertThat(entity.getWeight()).isEqualTo(api.getWeight());

    Product api2 = mapper.entityToApi(entity);

    assertThat(api2.getProductId()).isEqualTo(api.getProductId());
    assertThat(api2.getName()).isEqualTo(api.getName());
    assertThat(api2.getWeight()).isEqualTo(api.getWeight());
    assertThat(api2.getServiceAddress()).isNull();
  }
}
