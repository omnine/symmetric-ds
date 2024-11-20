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

public class MaxDataToRouteMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<String> channelIdList = new ArrayList<>();
      Map<String, Channel> channelMap = this.a.getConfigurationService().getChannels(false);
      int minMaxDataToRoute = Integer.MAX_VALUE;
      long threshold = monitor.getThreshold();

      for (Channel channel : channelMap.values()) {
         String channelId = channel.getChannelId();
         if (!channelId.equals("config")
            && !channelId.equals("dynamic")
            && !channelId.equals("filesync")
            && !channelId.equals("filesync_reload")
            && !channelId.equals("heartbeat")
            && !channelId.equals("monitor")
            && !channelId.equals("reload")) {
            int maxDataToRoute = channel.getMaxDataToRoute();
            if (maxDataToRoute < minMaxDataToRoute) {
               minMaxDataToRoute = maxDataToRoute;
            }

            if ((long)channel.getMaxDataToRoute() <= threshold) {
               channelIdList.add(channelId);
               String problemDescription = "Channel '"
                  + channelId
                  + "' has a low max data to route of "
                  + maxDataToRoute
                  + ".  This can make routing less efficient and slower in some cases.  It is usually set to 100,000 or higher.";
               String actionDescription = "Increase the max data to route to 100,000 or more.";
               fT recommendation = new fT(problemDescription, actionDescription, true);
               recommendation.a("channelId", channelId);
               List<fT.a> options = new ArrayList<>();
               options.add(
                  recommendation.new a(1, "Increase the max data to route to " + (threshold + 1L) + " for the '" + channelId + "' channel", threshold + 1L)
               );
               recommendation.a(options);
            }
         }
      }

      event.setValue((long)minMaxDataToRoute);
      int channelIdCount = channelIdList.size();
      if (channelIdCount > 0) {
         String problemDescription;
         if (channelIdCount == 1) {
            problemDescription = "The '" + channelIdList.get(0) + "' channel has a low max data to route of " + minMaxDataToRoute;
         } else {
            problemDescription = "The following channels have a low max data to route: " + channelIdList;
         }

         problemDescription = problemDescription + ".  This can make routing less efficient and slower in some cases.";
         String actionDescription = "Increase the max data to route to " + (threshold + 1L) + " or more.";
         fT recommendation = new fT(problemDescription, actionDescription, true);
         List<fT.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, "Increase the max data to route to a minimum of " + (threshold + 1L) + " for all channels", threshold + 1L));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, fT recommendation) {
      int minMaxDataToRoute = (int)event.getThreshold() + 1;
      if (event.getApprovedOption() == 1) {
         minMaxDataToRoute = (int)recommendation.a(1);
      }

      List<String> modifiedChannelIdList = new ArrayList<>();
      IConfigurationService configService = this.a.getConfigurationService();

      for (Channel channel : configService.getChannels(true).values()) {
         if (channel.getMaxDataToRoute() < minMaxDataToRoute) {
            channel.setMaxDataToRoute(minMaxDataToRoute);
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
      return "maxDataToRoute";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
