package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import java.util.ArrayList;
import java.util.List;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.NodeChannel;
import org.jumpmind.symmetric.service.IConfigurationService;

public class ChannelSuspendMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<NodeChannel> nodeChannels = this.a.getConfigurationService().getNodeChannels(this.a.getNodeId(), false);
      List<String> suspendedOrIgnoredChannelIdList = new ArrayList<>();

      for (NodeChannel nodeChannel : nodeChannels) {
         String channelId = nodeChannel.getChannelId();
         if (nodeChannel.isSuspendEnabled() || nodeChannel.isIgnoreEnabled()) {
            suspendedOrIgnoredChannelIdList.add(channelId);
         }
      }

      int channelCount = suspendedOrIgnoredChannelIdList.size();
      event.setValue((long)channelCount);
      if (channelCount > 0) {
         String problemDescription;
         String actionDescription;
         if (channelCount == 1) {
            String channelId = suspendedOrIgnoredChannelIdList.get(0);
            problemDescription = "The '" + channelId + "' channel is suspended or ignored.";
            actionDescription = "Unflag the '" + channelId + "' channel in the " + this.a.getParameterService().getTablePrefix() + "_node_channel_ctl table.";
         } else {
            problemDescription = "The following channels are suspended or ignored: " + suspendedOrIgnoredChannelIdList + ".";
            actionDescription = "Unflag the channels in the " + this.a.getParameterService().getTablePrefix() + "_node_channel_ctl table.";
         }

         Recommendation recommendation = new Recommendation(problemDescription, actionDescription, true);
         List<Recommendation.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, "Unsuspend and unignore all channels"));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      List<String> modifiedNodeChannelList = new ArrayList<>();
      IConfigurationService configService = this.a.getConfigurationService();
      String nodeId = this.a.getNodeId();

      for (NodeChannel nodeChannel : configService.getNodeChannels(nodeId, false)) {
         if (nodeChannel.isSuspendEnabled() || nodeChannel.isIgnoreEnabled()) {
            nodeChannel.setSuspendEnabled(false);
            nodeChannel.setIgnoreEnabled(false);
            configService.saveNodeChannel(nodeChannel, true);
            modifiedNodeChannelList.add(nodeChannel.toString());
         }
      }

      IConsoleEventService consoleEventService = (IConsoleEventService)this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
      consoleEventService.addEvent(new ConsoleEvent(event.getApprovedBy(), "Node Channel Modified", nodeId, nodeId, null, modifiedNodeChannelList.toString()));
      return true;
   }

   @Override
   public String b() {
      return "channelSuspend";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
