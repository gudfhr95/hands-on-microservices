package com.example.api.composite.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
public class ServiceAddress {

  private final String cmp;
  private final String pro;
  private final String rev;
  private final String rec;
}
