package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.statistic.IThroughputStatisticManager;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public class LicenseRowsMonitor extends StackMonitor implements IBuiltInExtensionPoint {
   @Override
   public MonitorEvent a(Monitor monitor) {
      K licenseService = (K)this.c.getExtensionService().getExtensionPoint(K.class);
      IThroughputStatisticManager statisticsManager = (IThroughputStatisticManager)this.c.getStatisticManager();
      MonitorEvent event = new MonitorEvent();
      int maxRows = licenseService.c();
      long rows = statisticsManager.getCdcRowsIn() + statisticsManager.getCdcRowsOut();
      if (maxRows > 0) {
         event.setValue(rows * 100L / (long)maxRows);
         event.setDetails("Using " + rows + " out of " + maxRows + " rows");
      } else {
         event.setValue(0L);
      }

      return event;
   }

   @Override
   public String b() {
      return "licenseRows";
   }
}
