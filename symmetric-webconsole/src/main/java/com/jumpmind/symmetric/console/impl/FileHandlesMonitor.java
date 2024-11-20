package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public class FileHandlesMonitor implements MonitorExtension, IBuiltInExtensionPoint {
   @Override
   public String b() {
      return "fileHandles";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();

      try {
         ObjectName oName = new ObjectName("java.lang:type=OperatingSystem");
         MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
         int openFiles = Integer.parseInt(mbeanServer.getAttribute(oName, "OpenFileDescriptorCount").toString());
         int maxCount = Integer.parseInt(mbeanServer.getAttribute(oName, "MaxFileDescriptorCount").toString());
         int percent = (int)((float)openFiles * 100.0F / (float)maxCount);
         event.setValue((long)percent);
         return event;
      } catch (Exception var8) {
         event.setValue(0L);
         return event;
      }
   }

   @Override
   public boolean a() {
      return false;
   }
}
