package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.security.cert.X509Certificate;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public class CertExpireMonitor extends StackMonitor implements IBuiltInExtensionPoint {
   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      X509Certificate cert = this.c.getSecurityService().getCurrentSslCert();
      long notBefore = cert.getNotBefore().getTime();
      long notAfter = cert.getNotAfter().getTime();
      if (notBefore != -1L && notAfter != -1L) {
         long percentage = (System.currentTimeMillis() - notBefore) * 100L / (notAfter - notBefore);
         event.setValue(percentage);
         event.setDetails("Certificate expires in " + com.jumpmind.symmetric.console.ui.common.am.formatDurationFull(notAfter - System.currentTimeMillis()));
      } else {
         event.setValue(0L);
      }

      return event;
   }

   @Override
   public String b() {
      return "certExpire";
   }
}
