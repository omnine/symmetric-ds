package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.util.List;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.service.IParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OfflineNodesMonitor implements MonitorExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   protected final Logger a = LoggerFactory.getLogger(this.getClass());
   protected INodeService b;
   protected IParameterService c;

   @Override
   public String b() {
      return "offlineNodes";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      int minutesBeforeNodeIsOffline = this.c.getInt("console.report.as.offline.minutes", 1440);
      MonitorEvent event = new MonitorEvent();
      List<String> offlineNodes = this.b.findOfflineNodeIds((long)minutesBeforeNodeIsOffline);
      event.setValue((long)offlineNodes.size());
      event.setDetails(this.a(offlineNodes));
      return event;
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.b = engine.getNodeService();
      this.c = engine.getParameterService();
   }

   protected String a(List<String> offlineNodes) {
      String result = null;

      try {
         result = com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(offlineNodes);
      } catch (Exception var4) {
         this.a.warn("Unable to convert list of offline nodes to JSON", var4);
      }

      return result;
   }
}
