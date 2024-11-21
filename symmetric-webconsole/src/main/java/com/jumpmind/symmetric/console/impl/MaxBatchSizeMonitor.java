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

public class MaxBatchSizeMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<String> channelIdList = new ArrayList<>();
      Map<String, Channel> channelMap = this.a.getConfigurationService().getChannels(false);
      int maxMaxBatchSize = 0;
      long threshold = monitor.getThreshold();

      for (Channel channel : channelMap.values()) {
         int maxBatchSize = channel.getMaxBatchSize();
         if (maxBatchSize > maxMaxBatchSize) {
            maxMaxBatchSize = maxBatchSize;
         }

         if ((long)maxBatchSize >= threshold) {
            channelIdList.add(channel.getChannelId());
         }
      }

      event.setValue((long)maxMaxBatchSize);
      int channelIdCount = channelIdList.size();
      if (channelIdCount > 0) {
         String problemDescription;
         if (channelIdCount == 1) {
            problemDescription = "The '" + channelIdList.get(0) + "' channel has a high max batch size of " + maxMaxBatchSize;
         } else {
            problemDescription = "The following channels have a high max batch size: " + channelIdList;
         }

         problemDescription = problemDescription + ".  A high commit size can cause transactional overhead and slowness for smaller databases.";
         String actionDescription = "Reduce the max batch size to " + (threshold - 1L) + " or less.";
         Recommendation recommendation = new Recommendation(problemDescription, actionDescription, true);
         List<Recommendation.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, "Reduce the max batch size to a maximum of " + (threshold - 1L) + " for all channels", threshold - 1L));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      int maxMaxBatchSize = (int)event.getThreshold() - 1;
      if (event.getApprovedOption() == 1) {
         maxMaxBatchSize = (int)recommendation.a(event.getApprovedOption());
      }

      IConfigurationService configService = this.a.getConfigurationService();
      List<String> modifiedChannelIdList = new ArrayList<>();

      for (Channel channel : configService.getChannels(true).values()) {
         if (channel.getMaxBatchSize() > maxMaxBatchSize) {
            channel.setMaxBatchSize(maxMaxBatchSize);
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
      return "maxBatchSize";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
