package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.AbstractBatch;
import org.jumpmind.symmetric.model.IncomingBatch;
import org.jumpmind.symmetric.model.OutgoingBatch;
import org.jumpmind.symmetric.model.OutgoingBatches;
import org.jumpmind.symmetric.service.IIncomingBatchService;
import org.jumpmind.symmetric.service.IOutgoingBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchErrorMonitor implements MonitorExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   protected final Logger a = LoggerFactory.getLogger(this.getClass());
   protected IOutgoingBatchService b;
   protected IIncomingBatchService c;

   @Override
   public String b() {
      return "batchError";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      int outgoingErrorCount = 0;
      MonitorEvent event = new MonitorEvent();
      List<OutgoingBatch> outgoingErrors = new ArrayList<>();
      List<IncomingBatch> incomingErrors = new ArrayList<>();
      OutgoingBatches outgoingBatches = this.b.getOutgoingBatchErrors(1000);

      for (OutgoingBatch batch : outgoingBatches.getBatches()) {
         outgoingErrorCount++;
         this.a(batch);
         outgoingErrors.add(batch);
      }

      int incomingErrorCount = 0;

      for (IncomingBatch batch : this.c.findIncomingBatchErrors(1000)) {
         incomingErrorCount++;
         this.a(batch);
         incomingErrors.add(batch);
      }

      event.setValue((long)(outgoingErrorCount + incomingErrorCount));
      BatchErrors wrapper = new BatchErrors();
      if (outgoingErrors.size() > 0) {
         wrapper.a(outgoingErrors);
      }

      if (incomingErrors.size() > 0) {
         wrapper.b(incomingErrors);
      }

      event.setDetails(this.a(wrapper));
      return event;
   }

   protected void a(AbstractBatch batch) {
      if (batch.getLastUpdatedTime() instanceof Timestamp lastUpdatedTime) {
         batch.setLastUpdatedTime(new Date(lastUpdatedTime.getTime()));
      }

      if (batch.getCreateTime() instanceof Timestamp createTime) {
         batch.setCreateTime(new Date(createTime.getTime()));
      }

      if (batch instanceof OutgoingBatch outgoingBatch) {
         if (outgoingBatch.getExtractStartTime() instanceof Timestamp extractStartTime) {
            outgoingBatch.setExtractStartTime(new Date(extractStartTime.getTime()));
         }

         if (outgoingBatch.getTransferStartTime() instanceof Timestamp transferStartTime) {
            outgoingBatch.setTransferStartTime(new Date(transferStartTime.getTime()));
         }

         if (outgoingBatch.getLoadStartTime() instanceof Timestamp loadStartTime) {
            outgoingBatch.setLoadStartTime(new Date(loadStartTime.getTime()));
         }
      }
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.b = engine.getOutgoingBatchService();
      this.c = engine.getIncomingBatchService();
   }

   protected String a(BatchErrors details) {
      String result = null;

      try {
         result = com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(details);
      } catch (Exception var4) {
         this.a.warn("Unable to convert batch errors to JSON", var4);
      }

      return result;
   }
}
