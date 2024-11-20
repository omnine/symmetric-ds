package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public class JVM64BitMonitor implements InsightMonitor, IBuiltInExtensionPoint {
   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      String jvmBits = System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode", ""));
      String osArch = System.getProperty("os.arch", "");
      if (osArch.contains("64") && jvmBits.equals("32")) {
         event.setValue(1L);
         String problemDescription = "The operating system is 64-bit but the JVM is 32-bit.";
         String actionDescription = "Install a 64-bit JVM to access more memory.";
         event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(new fT(problemDescription, actionDescription, false)));
      } else {
         event.setValue(0L);
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, fT recommendation) {
      return true;
   }

   @Override
   public String b() {
      return "jvm64Bit";
   }

   @Override
   public boolean a() {
      return false;
   }
}
