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
import org.jumpmind.symmetric.model.Channel;
import org.jumpmind.symmetric.model.FileTrigger;
import org.jumpmind.symmetric.model.Trigger;
import org.jumpmind.symmetric.service.IConfigurationService;
import org.jumpmind.symmetric.service.IDataLoaderService;
import org.jumpmind.symmetric.service.IFileSyncService;
import org.jumpmind.symmetric.service.ITriggerRouterService;
import org.jumpmind.symmetric.service.impl.DataLoaderService.ConflictNodeGroupLink;

public class MaxChannelsMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<Channel> dataChannelList = new ArrayList<>();
      List<Channel> fileSyncChannelList = new ArrayList<>();

      for (Channel channel : this.a.getConfigurationService().getChannels(false).values()) {
         String channelId = channel.getChannelId();
         if (!channelId.equals("config")
            && !channelId.equals("default")
            && !channelId.equals("dynamic")
            && !channelId.equals("filesync")
            && !channelId.equals("filesync_reload")
            && !channelId.equals("heartbeat")
            && !channelId.equals("monitor")
            && !channelId.equals("reload")) {
            if (channel.isFileSyncFlag()) {
               fileSyncChannelList.add(channel);
            } else {
               dataChannelList.add(channel);
            }
         }
      }

      int dataChannelCount = dataChannelList.size() + 2;
      int fileSyncChannelCount = fileSyncChannelList.size() + 2;
      event.setValue(dataChannelCount > fileSyncChannelCount ? (long)dataChannelCount : (long)fileSyncChannelCount);
      long threshold = monitor.getThreshold();
      boolean tooManyDataChannels = (long)dataChannelCount >= threshold;
      boolean tooManyFileSyncChannels = (long)fileSyncChannelCount >= threshold;
      if (tooManyDataChannels || tooManyFileSyncChannels) {
         String problemDescription = "There are ";
         if (tooManyDataChannels) {
            problemDescription = problemDescription + dataChannelCount + " data channels";
            if (tooManyFileSyncChannels) {
               problemDescription = problemDescription + " and " + fileSyncChannelCount + " file sync channels";
            }
         } else {
            problemDescription = problemDescription + fileSyncChannelCount + " file sync channels";
         }

         problemDescription = problemDescription + ".  A large number of channels can slow down routing.";
         String actionDescription = "Consolidate triggers to a smaller number of channels.";
         Recommendation recommendation = new Recommendation(problemDescription, actionDescription, true);
         List<Recommendation.a> options = new ArrayList<>();
         String optionDescription = "";
         if (tooManyDataChannels) {
            List<String> dataChannelIdsToReplaceList = new ArrayList<>();
            List<String> dataReloadChannelIdsToReplaceList = new ArrayList<>();
            dataChannelList.sort((c0, c1) -> Integer.compare(c1.getProcessingOrder(), c0.getProcessingOrder()));

            for (int i = 0; (long)i <= (long)dataChannelCount - threshold && i < dataChannelList.size(); i++) {
               Channel dataChannel = dataChannelList.get(i);
               if (dataChannel.isReloadFlag()) {
                  dataReloadChannelIdsToReplaceList.add(dataChannel.getChannelId());
               } else {
                  dataChannelIdsToReplaceList.add(dataChannel.getChannelId());
               }
            }

            if (!dataChannelIdsToReplaceList.isEmpty()) {
               optionDescription = "Replace the following channels with the 'default' channel: " + dataChannelIdsToReplaceList + ". ";
               recommendation.a("dataChannelIds", dataChannelIdsToReplaceList);
            }

            if (!dataReloadChannelIdsToReplaceList.isEmpty()) {
               optionDescription = optionDescription + "Replace the following channels with the 'reload' channel: " + dataReloadChannelIdsToReplaceList + ". ";
               recommendation.a("dataReloadChannelIds", dataReloadChannelIdsToReplaceList);
            }
         }

         if (tooManyFileSyncChannels) {
            List<String> fileSyncChannelIdsToReplaceList = new ArrayList<>();
            List<String> fileSyncReloadChannelIdsToReplaceList = new ArrayList<>();
            fileSyncChannelList.sort((c0, c1) -> Integer.compare(c1.getProcessingOrder(), c0.getProcessingOrder()));

            for (int ix = 0; (long)ix <= (long)fileSyncChannelCount - threshold && ix < fileSyncChannelList.size(); ix++) {
               Channel fileSyncChannel = fileSyncChannelList.get(ix);
               if (fileSyncChannel.isReloadFlag()) {
                  fileSyncReloadChannelIdsToReplaceList.add(fileSyncChannel.getChannelId());
               } else {
                  fileSyncChannelIdsToReplaceList.add(fileSyncChannel.getChannelId());
               }
            }

            if (!fileSyncChannelIdsToReplaceList.isEmpty()) {
               optionDescription = optionDescription + "Replace the following channels with the 'filesync' channel: " + fileSyncChannelIdsToReplaceList + ". ";
               recommendation.a("fileSyncChannelIds", fileSyncChannelIdsToReplaceList);
            }

            if (!fileSyncReloadChannelIdsToReplaceList.isEmpty()) {
               optionDescription = optionDescription
                  + "Replace the following channels with the 'filesync_reload' channel: "
                  + fileSyncReloadChannelIdsToReplaceList
                  + ".";
               recommendation.a("fileSyncReloadChannelIds", fileSyncReloadChannelIdsToReplaceList);
            }
         }

         options.add(recommendation.new a(1, optionDescription.trim()));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      List<String> dataChannelIdsToReplaceList = (List<String>)recommendation.c("dataChannelIds");
      List<String> dataReloadChannelIdsToReplaceList = (List<String>)recommendation.c("dataReloadChannelIds");
      List<String> fileSyncChannelIdsToReplaceList = (List<String>)recommendation.c("fileSyncChannelIds");
      List<String> fileSyncReloadChannelIdsToReplaceList = (List<String>)recommendation.c("fileSyncReloadChannelIds");
      List<ConflictNodeGroupLink> conflictList = this.a.getDataLoaderService().getConflictSettingsNodeGroupLinks();
      List<Trigger> triggerList = this.a.getTriggerRouterService().getTriggers();
      List<FileTrigger> fileTriggerList = this.a.getFileSyncService().getFileTriggers();
      this.a(dataChannelIdsToReplaceList, "default", conflictList, triggerList, null, event.getApprovedBy());
      this.a(dataReloadChannelIdsToReplaceList, "reload", conflictList, triggerList, null, event.getApprovedBy());
      this.a(fileSyncChannelIdsToReplaceList, "filesync", conflictList, null, fileTriggerList, event.getApprovedBy());
      this.a(fileSyncReloadChannelIdsToReplaceList, "filesync_reload", conflictList, null, fileTriggerList, event.getApprovedBy());
      return true;
   }

   private void a(
      List<String> channelIdsToReplaceList,
      String replacementChannelId,
      List<ConflictNodeGroupLink> conflictList,
      List<Trigger> triggerList,
      List<FileTrigger> fileTriggerList,
      String userId
   ) {
      if (channelIdsToReplaceList != null) {
         IDataLoaderService dataLoaderService = this.a.getDataLoaderService();
         ITriggerRouterService triggerRouterService = this.a.getTriggerRouterService();
         IFileSyncService fileSyncService = this.a.getFileSyncService();
         IConfigurationService configService = this.a.getConfigurationService();
         List<String> modifiedConflictIdList = new ArrayList<>();
         List<String> modifiedTriggerIdList = new ArrayList<>();
         List<String> modifiedFileTriggerIdList = new ArrayList<>();

         for (String channelId : channelIdsToReplaceList) {
            for (ConflictNodeGroupLink conflict : conflictList) {
               if (channelId.equals(conflict.getTargetChannelId())) {
                  conflict.setTargetChannelId(replacementChannelId);
                  dataLoaderService.save(conflict);
                  modifiedConflictIdList.add(conflict.getConflictId());
               }
            }

            if (triggerList != null) {
               for (Trigger trigger : triggerList) {
                  boolean triggerModified = false;
                  if (trigger.getChannelId().equals(channelId)) {
                     trigger.setChannelId(replacementChannelId);
                     triggerModified = true;
                  }

                  if (trigger.getReloadChannelId().equals(channelId)) {
                     trigger.setReloadChannelId(replacementChannelId);
                     triggerModified = true;
                  }

                  if (triggerModified) {
                     triggerRouterService.saveTrigger(trigger);
                     modifiedTriggerIdList.add(trigger.getTriggerId());
                  }
               }
            }

            if (fileTriggerList != null) {
               for (FileTrigger fileTrigger : fileTriggerList) {
                  boolean fileTriggerModified = false;
                  if (fileTrigger.getChannelId().equals(channelId)) {
                     fileTrigger.setChannelId(replacementChannelId);
                     fileTriggerModified = true;
                  }

                  if (fileTrigger.getReloadChannelId().equals(channelId)) {
                     fileTrigger.setReloadChannelId(replacementChannelId);
                     fileTriggerModified = true;
                  }

                  if (fileTriggerModified) {
                     fileSyncService.saveFileTrigger(fileTrigger);
                     modifiedFileTriggerIdList.add(fileTrigger.getTriggerId());
                  }
               }
            }

            Channel channel = configService.getChannel(channelId);
            if (channel != null) {
               configService.deleteChannel(channel);
            }
         }

         IConsoleEventService consoleEventService = (IConsoleEventService)this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
         String nodeId = this.a.getNodeId();
         if (!modifiedConflictIdList.isEmpty()) {
            consoleEventService.addEvent(new ConsoleEvent(userId, "Conflict Modified", nodeId, nodeId, null, modifiedConflictIdList.toString()));
         }

         if (!modifiedTriggerIdList.isEmpty()) {
            consoleEventService.addEvent(new ConsoleEvent(userId, "Table Trigger Modified", nodeId, nodeId, null, modifiedTriggerIdList.toString()));
         }

         if (!modifiedFileTriggerIdList.isEmpty()) {
            consoleEventService.addEvent(new ConsoleEvent(userId, "File Trigger Modified", nodeId, nodeId, null, modifiedFileTriggerIdList.toString()));
         }

         consoleEventService.addEvent(new ConsoleEvent(userId, "Channel Deleted", nodeId, nodeId, null, channelIdsToReplaceList.toString()));
      }
   }

   @Override
   public String b() {
      return "maxChannels";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
