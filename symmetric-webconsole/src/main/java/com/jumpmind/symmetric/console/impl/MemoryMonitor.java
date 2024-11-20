package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import javax.management.ObjectName;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryMonitor extends StackMonitor implements IBuiltInExtensionPoint {
   protected final Logger d = LoggerFactory.getLogger(this.getClass());
   protected MemoryPoolMXBean e;

   @Override
   public String b() {
      return "memory";
   }

   public MemoryMonitor() {
      for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
         if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
            this.e = pool;
            break;
         }
      }

      if (this.e == null) {
         this.d.warn("Unable to find tenured memory pool");
      }
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      long usage = 0L;
      if (this.e != null) {
         usage = (long)((double)this.e.getUsage().getUsed() / (double)this.e.getUsage().getMax() * 100.0);
      }

      event.setValue(usage);
      event.setDetails(this.a(monitor.getMonitorId()));
      return event;
   }

   public String a(String monitorId) {
      long maxMemory = this.e.getUsage().getMax();
      long usedMemory = this.e.getUsage().getUsed();
      StringBuilder text = new StringBuilder("Memory threshold exceeded, " + usedMemory + " of " + maxMemory);

      try {
         String histogram = (String)ManagementFactory.getPlatformMBeanServer()
            .invoke(new ObjectName("com.sun.management:type=DiagnosticCommand"), "gcClassHistogram", new Object[]{null}, new String[]{"[Ljava.lang.String;"});
         text.append(System.lineSeparator() + "Top 10 classes sorted by memory usage:" + System.lineSeparator());
         int endIndex = StringUtils.ordinalIndexOf(histogram, "\n", 12);
         if (endIndex > 0) {
            text.append(histogram.substring(0, endIndex));
         } else {
            text.append(histogram);
         }
      } catch (Exception var9) {
         this.d.warn("Failed to generate details for the '" + monitorId + "' monitor", var9);
      }

      return text.toString();
   }

   @Override
   public boolean a() {
      return false;
   }
}
