package com.jumpmind.symmetric.console.impl;

import java.util.ArrayList;
import java.util.List;
import org.jumpmind.symmetric.model.IncomingBatch;
import org.jumpmind.symmetric.model.OutgoingBatch;

public class BatchErrors implements Comparable<Object> {
   List<OutgoingBatch> a;
   List<IncomingBatch> b;

   public BatchErrors() {
      this.a = new ArrayList<>();
      this.b = new ArrayList<>();
   }

   public BatchErrors(List<OutgoingBatch> outgoingBatch, List<IncomingBatch> incomingBatch) {
      this.a = outgoingBatch;
      this.b = incomingBatch;
   }

   public List<OutgoingBatch> a() {
      return this.a;
   }

   public void a(List<OutgoingBatch> outgoingErrors) {
      this.a = outgoingErrors;
   }

   public List<IncomingBatch> b() {
      return this.b;
   }

   public void b(List<IncomingBatch> incomingErrors) {
      this.b = incomingErrors;
   }

   @Override
   public int compareTo(Object o) { return 0;   }
}
