package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.statistic.JobStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobErrorMonitor extends StackMonitor implements IBuiltInExtensionPoint {
   protected final Logger d = LoggerFactory.getLogger(this.getClass());

   @Override
   public String b() {
      return "jobError";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<JobStats> jobStatsList = this.c.getStatisticService().getJobStatsForNode(this.c.getNodeId());
      if (!jobStatsList.isEmpty()) {
         Map<String, JobStats> statByJobMap = new HashMap<>();

         for (JobStats stats : jobStatsList) {
            statByJobMap.putIfAbsent(stats.getJobName(), stats);
         }

         String details = "";
         int errorCount = 0;

         for (JobStats jobStats : statByJobMap.values()) {
            if (jobStats.isErrorFlag()) {
               details = details + "Error for job " + jobStats.getJobName() + ": " + jobStats.getErrorMessage();
               errorCount++;
            }
         }

         if (errorCount > 0) {
            event.setValue((long)errorCount);
            event.setDetails(details);
         }
      }

      return event;
   }
}
