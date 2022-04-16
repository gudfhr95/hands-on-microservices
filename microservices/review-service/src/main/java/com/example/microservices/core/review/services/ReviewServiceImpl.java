package com.example.microservices.core.review.services;

import com.example.api.core.review.Review;
import com.example.api.core.review.ReviewService;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewServiceImpl implements ReviewService {

  private final ServiceUtil serviceUtil;

  @Autowired
  public ReviewServiceImpl(ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
  }

  @Override
  public List<Review> getReviews(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    if (productId == 213) {
      return new ArrayList<>();
    }

    List<Review> list = new ArrayList<>();
    list.add(new Review(
        productId,
        1,
        "Author 1",
        "Subject 1",
        "Content 1",
        serviceUtil.getServiceAddress()
    ));
    list.add(new Review(
        productId,
        2,
        "Author 2",
        "Subject 2",
        "Content 2",
        serviceUtil.getServiceAddress()
    ));
    list.add(new Review(
        productId,
        3,
        "Author 3",
        "Subject 3",
        "Content 3",
        serviceUtil.getServiceAddress()
    ));

    return list;
  }
}
