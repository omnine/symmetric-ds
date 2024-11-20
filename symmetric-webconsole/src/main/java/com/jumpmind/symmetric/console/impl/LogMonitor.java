package com.jumpmind.symmetric.console.impl;

import com.google.gson.GsonBuilder;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.util.LogSummaryAppenderUtils;
import org.jumpmind.symmetric.util.SuperClassExclusion;
import org.jumpmind.util.LogSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogMonitor implements MonitorExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   protected final Logger a = LoggerFactory.getLogger(this.getClass());
   ISymmetricEngine b;

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.b = engine;
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      List<LogSummary> all = new ArrayList<>();
      MonitorEvent event = new MonitorEvent();
      if (monitor.getSeverityLevel() == 300) {
         all.addAll(LogSummaryAppenderUtils.getLogSummaryErrors(this.b.getEngineName()));
      } else if (monitor.getSeverityLevel() == 200) {
         all.addAll(LogSummaryAppenderUtils.getLogSummaryWarnings(this.b.getEngineName()));
      }

      Collections.sort(all);
      int count = 0;

      for (LogSummary logSummary : all) {
         count += logSummary.getCount();
      }

      event.setValue((long)all.size());
      event.setCount(count);
      String details = this.a(all);
      if ("interbase".equals(this.b.getDatabasePlatform().getName())) {
         while (details != null && details.length() > 4096 && all.size() > 1) {
            all.remove(all.size() - 1);
            details = this.a(all);
         }

         if (details != null && details.length() > 4096) {
            details = details.substring(0, 4096);
         }
      }

      event.setDetails(details);
      return event;
   }

   protected String a(List<LogSummary> logs) {
      String result = null;

      try {
         GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
         builder.addSerializationExclusionStrategy(new SuperClassExclusion());
         builder.addDeserializationExclusionStrategy(new SuperClassExclusion());
         builder.setDateFormat("MMM dd, yyyy, HH:mm:ss");
         result = builder.create().toJson(logs);
      } catch (Exception var4) {
         this.a.warn("Unable to convert list of logs to JSON", var4);
      }

      return result;
   }

   @Override
   public boolean a() {
      return true;
   }

   @Override
   public String b() {
      return "log";
   }
}
