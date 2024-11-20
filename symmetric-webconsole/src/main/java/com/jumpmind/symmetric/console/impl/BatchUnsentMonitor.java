package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.service.IOutgoingBatchService;

public class BatchUnsentMonitor implements MonitorExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   protected ISymmetricEngine a;

   @Override
   public String b() {
      return "batchUnsent";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      IOutgoingBatchService outgoingBatchService = this.a.getOutgoingBatchService();
      int unsentBatchCount = outgoingBatchService.countOutgoingBatchesUnsent();
      int minutesBeforeOffline = this.a.getParameterService().getInt("console.report.as.offline.minutes");

      for (String offlineNodeId : this.a.getNodeService().findOfflineNodeIds((long)minutesBeforeOffline)) {
         unsentBatchCount -= outgoingBatchService.countUnsentBatchesByTargetNode(offlineNodeId, true);
      }

      event.setValue((long)unsentBatchCount);
      return event;
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
