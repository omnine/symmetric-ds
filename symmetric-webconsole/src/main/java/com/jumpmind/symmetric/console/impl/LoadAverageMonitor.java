package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public class LoadAverageMonitor implements MonitorExtension, IBuiltInExtensionPoint {
   @Override
   public String b() {
      return "loadAverage";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
      event.setValue((long)osBean.getSystemLoadAverage());
      return event;
   }

   @Override
   public boolean a() {
      return false;
   }
}
