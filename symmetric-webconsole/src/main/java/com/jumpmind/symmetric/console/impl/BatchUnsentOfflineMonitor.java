package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.service.IOutgoingBatchService;

public class BatchUnsentOfflineMonitor implements MonitorExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   protected ISymmetricEngine a;

   @Override
   public String b() {
      return "batchUnsentOffline";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      IOutgoingBatchService outgoingBatchService = this.a.getOutgoingBatchService();
      int unsentBatchCount = 0;
      int minutesBeforeOffline = this.a.getParameterService().getInt("console.report.as.offline.minutes");

      for (String offlineNodeId : this.a.getNodeService().findOfflineNodeIds(minutesBeforeOffline)) {
         unsentBatchCount += outgoingBatchService.countUnsentBatchesByTargetNode(offlineNodeId, true);
      }

      event.setValue(unsentBatchCount);
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
