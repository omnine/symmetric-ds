package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.service.IRouterService;

public class DataUnroutedMonitor implements MonitorExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   protected IRouterService a;

   @Override
   public String b() {
      return "dataUnrouted";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      event.setValue(this.a.getUnroutedDataCount());
      return event;
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine.getRouterService();
   }
}
