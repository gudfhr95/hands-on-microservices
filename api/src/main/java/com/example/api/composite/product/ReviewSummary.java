package com.example.api.composite.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ReviewSummary {

  private final int reviewId;
  private final String author;
  private final String subject;
  private final String content;
}
