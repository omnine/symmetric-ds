package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Channel;
import org.jumpmind.symmetric.service.IConfigurationService;

public class MaxBatchToSendMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<String> channelIdList = new ArrayList<>();
      Map<String, Channel> channelMap = this.a.getConfigurationService().getChannels(false);
      int maxMaxBatchToSend = 0;
      long threshold = monitor.getThreshold();

      for (Channel channel : channelMap.values()) {
         int maxBatchToSend = channel.getMaxBatchToSend();
         if (maxBatchToSend > maxMaxBatchToSend) {
            maxMaxBatchToSend = maxBatchToSend;
         }

         if ((long)maxBatchToSend >= threshold) {
            channelIdList.add(channel.getChannelId());
         }
      }

      event.setValue((long)maxMaxBatchToSend);
      int channelIdCount = channelIdList.size();
      if (channelIdCount > 0) {
         String problemDescription;
         if (channelIdCount == 1) {
            problemDescription = "The '" + channelIdList.get(0) + "' channel has a high max batch to send of " + maxMaxBatchToSend;
         } else {
            problemDescription = "The following channels have a high max batch to send: " + channelIdList;
         }

         problemDescription = problemDescription + ".  This can slow down status updates and may result in lost acks in some environments.";
         String actionDescription = "Reduce the max batch to send to " + (threshold - 1L) + " or less.";
         fT recommendation = new fT(problemDescription, actionDescription, true);
         List<fT.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, "Reduce the max batch to send to a maximum of " + (threshold - 1L) + " for all channels", threshold - 1L));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, fT recommendation) {
      int maxMaxBatchToSend = (int)event.getThreshold() - 1;
      if (event.getApprovedOption() == 1) {
         maxMaxBatchToSend = (int)recommendation.a(event.getApprovedOption());
      }

      IConfigurationService configService = this.a.getConfigurationService();
      List<String> modifiedChannelIdList = new ArrayList<>();

      for (Channel channel : configService.getChannels(true).values()) {
         if (channel.getMaxBatchToSend() > maxMaxBatchToSend) {
            channel.setMaxBatchToSend(maxMaxBatchToSend);
            configService.saveChannel(channel, true);
            modifiedChannelIdList.add(channel.getChannelId());
         }
      }

      IConsoleEventService consoleEventService = (IConsoleEventService)this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
      String nodeId = this.a.getNodeId();
      consoleEventService.addEvent(new ConsoleEvent(event.getApprovedBy(), "Channel Modified", nodeId, nodeId, null, modifiedChannelIdList.toString()));
      return true;
   }

   @Override
   public String b() {
      return "maxBatchToSend";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
