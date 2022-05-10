package com.example.api.core.product;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface ProductService {

  @PostMapping(
      value = "/product",
      consumes = "application/json",
      produces = "application/json"
  )
  Product createProduct(@RequestBody Product body);

  @GetMapping(
      value = "/product/{productId}",
      produces = "application/json"
  )
  Product getProduct(@PathVariable int productId);

  @DeleteMapping(value = "/product/{productId}")
  void deleteProduct(@PathVariable int productId);
}
