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

public class ChannelsDisabledMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<String> disabledChannelIdList = new ArrayList<>();
      Map<String, Channel> channelMap = this.a.getConfigurationService().getChannels(false);

      for (Channel channel : channelMap.values()) {
         if (!channel.isEnabled()) {
            disabledChannelIdList.add(channel.getChannelId());
         }
      }

      int disabledChannelCount = disabledChannelIdList.size();
      event.setValue(disabledChannelCount);
      if (disabledChannelCount > 0) {
         String problemDescription;
         if (disabledChannelCount == 1) {
            problemDescription = "The '" + disabledChannelIdList.get(0) + "' channel is disabled.";
         } else {
            problemDescription = "The following channels are disabled: " + disabledChannelIdList + ".";
         }

         String actionDescription = "Enable all channels.";
         Recommendation recommendation = new Recommendation(problemDescription, actionDescription, true);
         List<Recommendation.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, actionDescription.substring(0, actionDescription.length() - 1)));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      List<String> modifiedChannelIdList = new ArrayList<>();
      IConfigurationService configService = this.a.getConfigurationService();

      for (Channel channel : configService.getChannels(true).values()) {
         if (!channel.isEnabled()) {
            channel.setEnabled(true);
            configService.saveChannel(channel, true);
            modifiedChannelIdList.add(channel.getChannelId());
         }
      }

      IConsoleEventService consoleEventService = this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
      String nodeId = this.a.getNodeId();
      consoleEventService.addEvent(new ConsoleEvent(event.getApprovedBy(), "Channel Modified", nodeId, nodeId, null, modifiedChannelIdList.toString()));
      return true;
   }

   @Override
   public String b() {
      return "channelsDisabled";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
