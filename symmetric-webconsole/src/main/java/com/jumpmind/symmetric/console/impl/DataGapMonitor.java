package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.service.IDataService;

public class DataGapMonitor implements MonitorExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   protected IDataService a;

   @Override
   public String b() {
      return "dataGap";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      event.setValue(this.a.countDataGaps());
      return event;
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine.getDataService();
   }
}
