package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.symmetric.service.IParameterService;
import org.jumpmind.symmetric.statistic.HostStats;

public class StrandedOrExpiredDataMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      int strandedDataCount = 0;
      int expiredDataCount = 0;

      for (HostStats stats : this.a
         .getStatisticService()
         .getHostStatsForPeriod(new Date(System.currentTimeMillis() - 14400000L), new Date(), this.a.getNodeId())) {
         strandedDataCount = (int)((long)strandedDataCount + stats.getPurgedStrandedDataRows());
         expiredDataCount = (int)((long)expiredDataCount + stats.getPurgedExpiredDataRows());
      }

      int totalCount = strandedDataCount + expiredDataCount;
      event.setValue((long)totalCount);
      if (totalCount > 0) {
         String problemDescription = "The Purge Outgoing job detected ";
         if (strandedDataCount == 1) {
            problemDescription = problemDescription + "1 row of stranded data ";
         } else if (strandedDataCount > 1) {
            problemDescription = problemDescription + strandedDataCount + " rows of stranded data ";
         }

         if (strandedDataCount > 0 && expiredDataCount > 0) {
            problemDescription = problemDescription + "and ";
         }

         if (expiredDataCount == 1) {
            problemDescription = problemDescription + "1 row of expired data";
         } else if (expiredDataCount > 1) {
            problemDescription = problemDescription + expiredDataCount + " rows of expired data";
         }

         problemDescription = problemDescription.trim() + ".";
         String actionDescription = "Increase the routing.stale.dataid.gap.time.ms parameter.";
         Recommendation recommendation = new Recommendation(problemDescription, actionDescription, true);
         List<Recommendation.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, "Increase the routing.stale.dataid.gap.time.ms parameter by 20 minutes"));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      IParameterService parameterService = this.a.getParameterService();
      Node identity = this.a.getNodeService().findIdentity();
      int currentValue = parameterService.getInt("routing.stale.dataid.gap.time.ms", 1200000);
      String userId = event.getApprovedBy();
      parameterService.saveParameter(identity.getExternalId(), identity.getNodeGroupId(), "routing.stale.dataid.gap.time.ms", currentValue + 1200000, userId);
      IConsoleEventService consoleEventService = (IConsoleEventService)this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
      String nodeId = this.a.getNodeId();
      consoleEventService.addEvent(new ConsoleEvent(userId, "Edit Parameter", nodeId, nodeId, null, "routing.stale.dataid.gap.time.ms"));
      return true;
   }

   @Override
   public String b() {
      return "strandedOrExpiredData";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
