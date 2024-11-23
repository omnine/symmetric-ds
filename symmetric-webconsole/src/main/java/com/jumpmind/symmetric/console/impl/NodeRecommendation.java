package com.jumpmind.symmetric.console.impl;

import com.google.gson.Gson;
import java.util.Date;

public class NodeRecommendation {
   private Recommendation a;
   private String b;
   private String c;
   private Date d;

   public NodeRecommendation(Recommendation recommendation, String nodeId, Date eventTime) {
      this(recommendation, nodeId, null, eventTime);
   }

   public NodeRecommendation(Recommendation recommendation, String nodeId, String nodeGroupId, Date eventTime) {
      this.a = (Recommendation)new Gson().fromJson(new Gson().toJson(recommendation), Recommendation.class);
      this.a.i();
      this.a.a(nodeId, eventTime);
      this.b = nodeId;
      this.c = nodeGroupId;
      this.d = eventTime;
   }

   public Recommendation a() {
      return this.a;
   }

   public String b() {
      return this.b;
   }

   public String c() {
      return this.c;
   }

   public Date d() {
      return this.d;
   }
}
