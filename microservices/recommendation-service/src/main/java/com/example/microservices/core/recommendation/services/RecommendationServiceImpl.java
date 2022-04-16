package com.example.microservices.core.recommendation.services;

import com.example.api.core.recommendation.Recommendation;
import com.example.api.core.recommendation.RecommendationService;
import com.example.util.http.ServiceUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

  private final ServiceUtil serviceUtil;

  @Autowired
  public RecommendationServiceImpl(ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {
    List<Recommendation> list = new ArrayList<>();
    list.add(new Recommendation(
        productId,
        1,
        "Author 1",
        1,
        "Content 1",
        serviceUtil.getServiceAddress()
    ));
    list.add(new Recommendation(
        productId,
        2,
        "Author 2",
        2,
        "Content 2",
        serviceUtil.getServiceAddress()
    ));
    list.add(new Recommendation(
        productId,
        3,
        "Author 3",
        3,
        "Content 3",
        serviceUtil.getServiceAddress()
    ));

    return list;
  }
}
