package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public class LicenseExpireMonitor extends StackMonitor implements IBuiltInExtensionPoint {
   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      L licenseKey = null;
      long notBefore = -1L;
      long notAfter = -1L;
      if (this.c != null) {
         String keyStr = this.c.getParameterService().getString(F.a);
         if (keyStr != null && !keyStr.trim().equals("")) {
            licenseKey = com.jumpmind.symmetric.console.impl.c.a(keyStr, L.class);
            if (licenseKey != null) {
               notBefore = licenseKey.g().getTime();
               notAfter = licenseKey.h().getTime();
            }
         }
      }

      if (notBefore != -1L && notAfter != -1L) {
         long percentage = (System.currentTimeMillis() - notBefore) * 100L / (notAfter - notBefore);
         event.setValue(percentage);
         event.setDetails("License expires in " + com.jumpmind.symmetric.console.ui.common.am.formatDurationFull(notAfter - System.currentTimeMillis()));
      } else {
         event.setValue(0L);
      }

      return event;
   }

   @Override
   public String b() {
      return "licenseExpire";
   }
}
