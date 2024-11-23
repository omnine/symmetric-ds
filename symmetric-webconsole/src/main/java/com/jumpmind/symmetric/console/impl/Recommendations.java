package com.jumpmind.symmetric.console.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

public class Recommendations extends ArrayList<Recommendation> {
   private static final long a = 1L;

   public boolean a(Recommendation recommendation) {
      if (!this.contains(recommendation)) {
         return super.add(recommendation);
      } else {
         Recommendation existingRecommendation = this.get(this.indexOf(recommendation));

         for (Entry<String, Date> eventTimeByNodeIdEntry : recommendation.h().entrySet()) {
            existingRecommendation.a(eventTimeByNodeIdEntry.getKey(), eventTimeByNodeIdEntry.getValue());
         }

         return true;
      }
   }

   public void a(NodeRecommendation nodeRecommendation) {
      if (nodeRecommendation != null) {
         Recommendation parentRecommendation = nodeRecommendation.a();

         for (Recommendation recommendation : this) {
            if (recommendation.equals(parentRecommendation)) {
               recommendation.a(nodeRecommendation.b(), nodeRecommendation.d());
               return;
            }
         }

         this.a(parentRecommendation);
      }
   }

   public void b(NodeRecommendation nodeRecommendation) {
      if (nodeRecommendation != null) {
         Recommendation parentRecommendation = nodeRecommendation.a();
         Recommendation recommendationToRemove = null;

         for (Recommendation recommendation : this) {
            if (recommendation.equals(parentRecommendation)) {
               recommendation.f(nodeRecommendation.b());
               if (recommendation.h().isEmpty()) {
                  recommendationToRemove = recommendation;
               }
               break;
            }
         }

         if (recommendationToRemove != null) {
            this.remove(recommendationToRemove);
         }
      }
   }
}
