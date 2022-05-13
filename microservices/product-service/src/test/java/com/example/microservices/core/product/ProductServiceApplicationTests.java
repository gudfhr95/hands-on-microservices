package com.example.microservices.core.product;

import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.example.api.core.product.Product;
import com.example.api.event.Event;
import com.example.microservices.core.product.persistence.ProductRepository;
import com.example.util.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTests {

  @Autowired
  private WebTestClient client;

  @Autowired
  private ProductRepository repository;

  @Autowired
  private Sink channels;

  private AbstractMessageChannel input = null;

  @BeforeEach
  void setupDb() {
    input = (AbstractMessageChannel) channels.input();
    repository.deleteAll().block();
  }

  @Test
  void getProductById() {
    int productId = 1;

    assertThat(repository.findByProductId(productId).block()).isNull();
    assertThat(repository.count().block()).isZero();

    sendCreateProductEvent(productId);

    assertThat(repository.findByProductId(productId).block()).isNotNull();
    assertThat(repository.count().block()).isOne();

    getAndVerifyProduct(productId, OK)
        .jsonPath("$.productId").isEqualTo(productId);
  }

  @Test
  void duplicateError() {
    int productId = 1;

    assertThat(repository.findByProductId(productId).block()).isNull();

    sendCreateProductEvent(productId);

    assertThat(repository.findByProductId(productId).block()).isNotNull();

    try {
      sendCreateProductEvent(productId);
      fail("Expected a MessagingException here!");
    } catch (MessagingException me) {
      if (me.getCause() instanceof InvalidInputException) {
        InvalidInputException iie = (InvalidInputException) me.getCause();
        assertThat(iie.getMessage()).isEqualTo("Duplicate key, Product Id: " + productId);
      } else {
        fail("Expected a InvalidInputException as the root cause!");
      }
    }
  }

  @Test
  void deleteProduct() {
    int productId = 1;

    sendCreateProductEvent(productId);
    assertThat(repository.findByProductId(productId).block()).isNotNull();

    sendDeleteProductEvent(productId);
    assertThat(repository.findByProductId(productId).block()).isNull();

    sendDeleteProductEvent(productId);
  }

  @Test
  void getProductInvalidParameterString() {
    getAndVerifyProduct("/no-integer", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/product/no-integer")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getProductNotFound() {
    int productIdNotFound = 13;

    getAndVerifyProduct(productIdNotFound, NOT_FOUND)
        .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
        .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
  }

  @Test
  void getProductInvalidParameterNegativeValue() {
    int productIdInvalid = -1;

    getAndVerifyProduct(-1, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
        .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(
      int productId,
      HttpStatus expectedStatus
  ) {
    return getAndVerifyProduct("/" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(
      String productIdPath,
      HttpStatus expectedStatus
  ) {
    return client.get()
        .uri("/product" + productIdPath)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private void sendCreateProductEvent(int productId) {
    Product product = new Product(productId, "Name " + productId, productId, "SA");
    Event<Integer, Product> event = new Event<>(CREATE, productId, product);
    input.send(new GenericMessage<>(event));
  }

  private void sendDeleteProductEvent(int productId) {
    Event<Integer, Product> event = new Event<>(DELETE, productId, null);
    input.send(new GenericMessage<>(event));
  }
}
