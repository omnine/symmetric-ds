package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.io.File;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;

public class DiskMonitor implements MonitorExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   protected File a;

   @Override
   public String b() {
      return "disk";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      event.setValue((long)((1.0 - (double)this.a.getUsableSpace() / (double)this.a.getTotalSpace()) * 100.0));
      return event;
   }

   @Override
   public boolean a() {
      return false;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = new File(engine.getParameterService().getTempDirectory());
   }
}
