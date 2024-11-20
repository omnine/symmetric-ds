package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import com.jumpmind.symmetric.console.service.IMonitorService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Channel;
import org.jumpmind.symmetric.service.IConfigurationService;
import org.jumpmind.symmetric.util.LogSummaryAppenderUtils;
import org.jumpmind.util.LogSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionResetMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;
   private Logger b = LoggerFactory.getLogger(this.getClass());

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      Date notBefore = new Date(0L);

      for (MonitorEvent resolvedEvent : ((IMonitorService)this.a.getExtensionService().getExtensionPoint(IMonitorService.class))
         .getMonitorEventsFiltered(Integer.MAX_VALUE, this.b(), 0, this.a.getNodeId(), true)) {
         Date lastUpdateTime = resolvedEvent.getLastUpdateTime();
         if (resolvedEvent.getMonitorId().equals(monitor.getMonitorId()) && resolvedEvent.getApprovedOption() > 0 && notBefore.before(lastUpdateTime)) {
            notBefore = lastUpdateTime;
         }
      }

      IConfigurationService configService = this.a.getConfigurationService();
      List<LogSummary> logSummaryList = LogSummaryAppenderUtils.getLogSummaryErrors(this.a.getEngineName());
      Map<String, String> channelIdMap = new HashMap<>();

      for (LogSummary logSummary : logSummaryList) {
         String message = logSummary.getMessage();
         if (notBefore.before(new Date(logSummary.getMostRecentTime()))
            && message != null
            && message.startsWith("Failed to extract batch")
            && (StringUtils.containsIgnoreCase(message, "Connection reset") || StringUtils.containsIgnoreCase(logSummary.getStackTrace(), "Connection reset"))) {
            int startIndex = message.indexOf("on channel '") + 12;
            int endIndex = message.indexOf("'", startIndex);
            if (startIndex > 11 && endIndex > startIndex) {
               String channelId = message.substring(startIndex, endIndex);
               Channel channel = configService.getChannel(channelId);
               if (channel != null && channel.getMaxBatchToSend() > 1) {
                  channelIdMap.put(channelId, String.valueOf(channel.getMaxBatchToSend() / 2));
               }
            }
         }
      }

      int channelIdCount = channelIdMap.size();
      event.setValue((long)channelIdCount);
      if (channelIdCount > 0) {
         String problemDescription;
         String optionDescription;
         if (channelIdCount == 1) {
            String channelId = channelIdMap.keySet().iterator().next();
            problemDescription = "A connection reset error occurred on the '" + channelId + "' channel.";
            optionDescription = "Reduce the max batch to send by 50% for the '" + channelId + "' channel";
         } else {
            problemDescription = "A connection reset error occurred on the following channels: " + channelIdMap.keySet() + ".";
            optionDescription = "Reduce the max batch to send by 50% for the following channels: " + channelIdMap.keySet();
         }

         String actionDescription = "Reduce the max batch to send by 50%.";
         fT recommendation = new fT(problemDescription, actionDescription, true);
         List<fT.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, optionDescription));
         recommendation.a(options);
         recommendation.a("channelIdMap", channelIdMap);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, fT recommendation) {
      Map<String, String> channelIdMap = (Map<String, String>)recommendation.c("channelIdMap");
      if (channelIdMap != null) {
         IConfigurationService configService = this.a.getConfigurationService();

         for (String channelId : channelIdMap.keySet()) {
            Channel channel = configService.getChannel(channelId);
            if (channel != null) {
               channel.setMaxBatchToSend(Integer.valueOf(channelIdMap.get(channelId)));
               configService.saveChannel(channel, true);
            }
         }

         IConsoleEventService consoleEventService = (IConsoleEventService)this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
         String nodeId = this.a.getNodeId();
         consoleEventService.addEvent(new ConsoleEvent(event.getApprovedBy(), "Channel Modified", nodeId, nodeId, null, channelIdMap.keySet().toString()));
         return true;
      } else {
         this.b.error("Failed to approve Connection Reset insight because channel ID map was null.");
         return false;
      }
   }

   @Override
   public String b() {
      return "connectionReset";
   }

   @Override
   public boolean a() {
      return false;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
