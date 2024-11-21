package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public class JVMCrashMonitor implements InsightMonitor, IBuiltInExtensionPoint {
   protected static final long a = 86400000L;
   protected final SimpleDateFormat b = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      File crashFile = null;
      int crashFileCount = 0;
      File[] files = new File(".").listFiles();

      for (File file : files) {
         if (file.isFile()
            && file.getName().startsWith("hs_err_pid")
            && file.getName().endsWith(".log")
            && file.lastModified() >= System.currentTimeMillis() - 86400000L) {
            crashFileCount++;
            if (crashFile == null || file.lastModified() > crashFile.lastModified()) {
               crashFile = file;
            }
         }
      }

      event.setValue((long)crashFileCount);
      if (crashFile != null) {
         String problemDescription = "Java crashed on "
            + this.b.format(new Date(crashFile.lastModified()))
            + ".  See latest crash file "
            + crashFile.getAbsolutePath()
            + " for details.";
         if (crashFileCount > 1) {
            problemDescription = problemDescription + "  There have been " + crashFileCount + " Java crashes in the last 24 hours.";
         }

         String actionDescription = "Update Java to the latest patch release.";
         event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(new Recommendation(problemDescription, actionDescription, false)));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      return true;
   }

   @Override
   public String b() {
      return "jvmCrash";
   }

   @Override
   public boolean a() {
      return false;
   }
}
