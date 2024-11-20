package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JVMOutOfMemoryMonitor implements InsightMonitor, IBuiltInExtensionPoint {
   protected static final long a = 86400000L;
   private Logger c = LoggerFactory.getLogger(this.getClass());
   protected final SimpleDateFormat b = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      String oomDateTime = null;
      int oomCount = 0;
      String recentDateTime = this.b.format(Long.valueOf(System.currentTimeMillis() - 86400000L));

      try (BufferedReader reader = new BufferedReader(new FileReader("log/wrapper.log"))) {
         String line = null;

         while ((line = reader.readLine()) != null) {
            if (line.contains("java.lang.OutOfMemoryError")) {
               String dateTime = line.substring(0, 19);
               if (dateTime.compareTo(recentDateTime) > 0) {
                  oomCount++;
                  if (oomDateTime == null || dateTime.compareTo(oomDateTime) > 0) {
                     oomDateTime = dateTime;
                  }
               }
            }
         }
      } catch (Exception var12) {
      }

      event.setValue((long)oomCount);
      if (oomDateTime != null) {
         String problemDescription = "Out of memory error on " + oomDateTime + ".";
         if (oomCount > 1) {
            problemDescription = problemDescription + "  There have been " + oomCount + " out of memory errors in the last 24 hours.";
         }

         String actionDescription = "Increase maximum memory by editing the wrapper.java.maxmemory property in conf/sym_service.conf and restarting SymmetricDS.";
         fT recommendation = new fT(problemDescription, actionDescription, true);
         List<fT.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, "Increase by 0.5 GB", 512L));
         options.add(recommendation.new a(2, "Increase by 1 GB", 1024L));
         options.add(recommendation.new a(3, "Increase by 1.5 GB", 1532L));
         options.add(recommendation.new a(4, "Increase by 2 GB", 2048L));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, fT recommendation) {
      long increaseAmount = 512L;
      if (event.getApprovedOption() > 1) {
         increaseAmount = recommendation.a(event.getApprovedOption());
      }

      File serviceConfFile = new File("conf/sym_service.conf");
      if (serviceConfFile.exists()) {
         StringBuilder fileContents = new StringBuilder(256);

         try (BufferedReader reader = new BufferedReader(new FileReader(serviceConfFile))) {
            String line = reader.readLine();

            do {
               if (line != null) {
                  if (StringUtils.containsIgnoreCase(line, "wrapper.java.maxmemory=")) {
                     int currentMaxMemory = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                     fileContents.append(String.format("%s%n", "wrapper.java.maxmemory=" + ((long)currentMaxMemory + increaseAmount)));
                  } else {
                     fileContents.append(String.format("%s%n", line));
                  }
               }

               line = reader.readLine();
            } while (line != null);

            reader.close();
         } catch (Exception var15) {
            this.c.error("Failed to read conf/sym_service.conf when approving JVM Out of Memory insight", var15);
            return false;
         }

         try (PrintWriter writer = new PrintWriter(serviceConfFile)) {
            writer.println(fileContents);
            writer.close();
         } catch (Exception var13) {
            this.c.error("Failed to write to conf/sym_service.conf when approving JVM Out of Memory insight", var13);
            return false;
         }

         System.exit(0);
         return true;
      } else {
         this.c.error("Failed to approve JVM Out of Memory insight. The conf/sym_service.conf file does not exist.");
         return false;
      }
   }

   @Override
   public String b() {
      return "jvmOutOfMemory";
   }

   @Override
   public boolean a() {
      return false;
   }
}
