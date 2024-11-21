package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;

public class JVMThreadsMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      Map<String, Integer> methodCounts = new HashMap<>();
      int blockedCount = 0;
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
      long[] threadIds = threadBean.getAllThreadIds();

      for (long threadId : threadIds) {
         ThreadInfo info = threadBean.getThreadInfo(threadId, 1);
         if (info != null) {
            StackTraceElement[] elements = info.getStackTrace();
            if (elements != null && elements.length > 0 && info.getThreadState() == State.RUNNABLE) {
               String methodName = elements[0].getClassName() + "." + elements[0].getMethodName() + "():" + elements[0].getLineNumber();
               if (!methodName.startsWith("java") && !methodName.startsWith("sun")) {
                  Integer count = methodCounts.get(methodName);
                  if (count == null) {
                     count = 1;
                  } else {
                     count = count + 1;
                  }

                  methodCounts.put(methodName, count);
               }
            }

            if (info.getThreadState() == State.BLOCKED) {
               blockedCount++;
            }
         }
      }

      int highestCount = 1;
      String methodNameWithHighestCount = null;

      for (String methodName : methodCounts.keySet()) {
         int count = methodCounts.get(methodName);
         if (count > highestCount) {
            highestCount = count;
            methodNameWithHighestCount = methodName;
         }
      }

      Recommendation recommendation = null;
      if (blockedCount <= highestCount && (blockedCount != 1 || highestCount != 1)) {
         if (highestCount > 1) {
            event.setValue((long)highestCount);
            String problemDescription;
            if (blockedCount >= highestCount - 1) {
               problemDescription = "There are "
                  + highestCount
                  + " threads calling the same method "
                  + methodNameWithHighestCount
                  + " because there is a lock.  This may suggest a bottleneck.";
            } else {
               problemDescription = "There are "
                  + highestCount
                  + " threads calling the same method "
                  + methodNameWithHighestCount
                  + ".  This may suggest a bottleneck.";
            }

            String actionDescription = "Take a support snapshot and send it to support@jumpmind.com.";
            recommendation = new Recommendation(problemDescription, actionDescription, true);
         } else {
            event.setValue(0L);
         }
      } else {
         event.setValue((long)blockedCount);
         String problemDescription = "There are " + blockedCount + " threads that are blocked waiting for a lock.  This may suggest a bottleneck.";
         String actionDescription = "Use the Manage -> JVM Threads screen to find blocked threads.";
         recommendation = new Recommendation(problemDescription, actionDescription, true);
      }

      if (recommendation != null) {
         List<Recommendation.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, "Take a support snapshot and restart SymmetricDS"));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      String snapshotName = this.a.snapshot(null).toString();
      IConsoleEventService consoleEventService = (IConsoleEventService)this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
      String nodeId = this.a.getNodeId();
      consoleEventService.addEvent(new ConsoleEvent(event.getApprovedBy(), "Take Snapshot", nodeId, nodeId, null, snapshotName));
      System.exit(0);
      return true;
   }

   @Override
   public String b() {
      return "jvmThreads";
   }

   @Override
   public boolean a() {
      return false;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
