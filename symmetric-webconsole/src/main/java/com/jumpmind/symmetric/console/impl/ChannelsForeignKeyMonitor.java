package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Trigger;
import org.jumpmind.symmetric.model.TriggerHistory;
import org.jumpmind.symmetric.service.ITriggerRouterService;

public class ChannelsForeignKeyMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<List<Table>> tablesGroupedByFkList = E.a(E.a(this.a));
      ITriggerRouterService triggerRouterService = this.a.getTriggerRouterService();
      List<TriggerHistory> historyList = triggerRouterService.getActiveTriggerHistories();
      Set<String> affectedChannelIdSet = new HashSet<>();
      Map<String, List<String>> tableNamesToMoveByChannelIdMap = new HashMap<>();
      int tablesOnDifferentChannelsCount = 0;

      for (List<Table> groupedTableList : tablesGroupedByFkList) {
         Map<String, List<String>> tableNamesByChannelIdMap = new HashMap<>();
         int configuredTableCount = 0;

         for (Table table : groupedTableList) {
            Trigger trigger = this.a(table.getFullyQualifiedTableName(), historyList);
            if (trigger != null) {
               String channelId = trigger.getChannelId();
               List<String> tableNamesByChannelIdList = tableNamesByChannelIdMap.get(channelId);
               if (tableNamesByChannelIdList == null) {
                  tableNamesByChannelIdList = new ArrayList<>();
               }

               tableNamesByChannelIdList.add(table.getFullyQualifiedTableName());
               tableNamesByChannelIdMap.put(channelId, tableNamesByChannelIdList);
               configuredTableCount++;
            }
         }

         if (tableNamesByChannelIdMap.size() > 1) {
            affectedChannelIdSet.addAll(tableNamesByChannelIdMap.keySet());
            Entry<String, List<String>> entryWithLargestTableCount = null;

            for (Entry<String, List<String>> tableNamesByChannelIdEntry : tableNamesByChannelIdMap.entrySet()) {
               if (entryWithLargestTableCount == null || tableNamesByChannelIdEntry.getValue().size() > entryWithLargestTableCount.getValue().size()) {
                  entryWithLargestTableCount = tableNamesByChannelIdEntry;
               }
            }

            tablesOnDifferentChannelsCount += configuredTableCount - entryWithLargestTableCount.getValue().size();
            String channelIdToMoveTo = entryWithLargestTableCount.getKey();
            List<String> tableNamesToMoveList = tableNamesToMoveByChannelIdMap.get(channelIdToMoveTo);
            if (tableNamesToMoveList == null) {
               tableNamesToMoveList = new ArrayList<>();
            }

            for (Entry<String, List<String>> tableNamesByChannelIdEntryx : tableNamesByChannelIdMap.entrySet()) {
               if (!tableNamesByChannelIdEntryx.getKey().equals(entryWithLargestTableCount.getKey())) {
                  tableNamesToMoveList.addAll(tableNamesByChannelIdEntryx.getValue());
               }
            }

            tableNamesToMoveByChannelIdMap.put(channelIdToMoveTo, tableNamesToMoveList);
         }
      }

      event.setValue((long)tablesOnDifferentChannelsCount);
      if (!tableNamesToMoveByChannelIdMap.isEmpty()) {
         String problemDescription = "There are tables with foreign key dependencies on " + affectedChannelIdSet.size() + " different channels.";
         String actionDescription = "We recommend moving the tables to the same channel to avoid constraint errors.";
         fT recommendation = new fT(problemDescription, actionDescription, true);
         List<fT.a> options = new ArrayList<>();
         String optionDescription = "";

         for (Entry<String, List<String>> tableNamesToMoveByChannelIdEntry : tableNamesToMoveByChannelIdMap.entrySet()) {
            String channelId = tableNamesToMoveByChannelIdEntry.getKey();
            List<String> tableNameList = tableNamesToMoveByChannelIdEntry.getValue();
            if (tableNameList.size() == 1) {
               optionDescription = optionDescription + "Move the following table to the '" + channelId + "' channel: " + tableNameList.get(0) + ". ";
            } else {
               optionDescription = optionDescription
                  + "Move the following "
                  + tableNameList.size()
                  + " tables to the '"
                  + channelId
                  + "' channel: "
                  + tableNameList
                  + ". ";
            }
         }

         options.add(recommendation.new a(1, optionDescription.trim()));
         recommendation.a(options);
         recommendation.a("tableNamesToMoveByChannelId", tableNamesToMoveByChannelIdMap);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, fT recommendation) {
      ITriggerRouterService triggerRouterService = this.a.getTriggerRouterService();
      IConsoleEventService consoleEventService = (IConsoleEventService)this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
      String nodeId = this.a.getNodeId();
      List<TriggerHistory> historyList = this.a.getTriggerRouterService().getActiveTriggerHistories();
      Map<String, List<String>> tableNamesToMoveByChannelIdMap = (Map<String, List<String>>)recommendation.c("tableNamesToMoveByChannelId");

      for (Entry<String, List<String>> tableNamesToMoveByChannelIdEntry : tableNamesToMoveByChannelIdMap.entrySet()) {
         String channelId = tableNamesToMoveByChannelIdEntry.getKey();

         for (String tableName : tableNamesToMoveByChannelIdEntry.getValue()) {
            Trigger trigger = this.a(tableName, historyList);
            if (trigger != null) {
               trigger.setChannelId(channelId);
               triggerRouterService.saveTrigger(trigger);
               consoleEventService.addEvent(new ConsoleEvent(event.getApprovedBy(), "Table Trigger Modified", nodeId, nodeId, null, trigger.getTriggerId()));
            }
         }
      }

      return true;
   }

   private Trigger a(String tableName, List<TriggerHistory> historyList) {
      IDatabasePlatform platform = this.a.getDatabasePlatform();
      String triggerId = null;

      for (TriggerHistory history : historyList) {
         String catalogName = history.getSourceCatalogName() != null ? history.getSourceCatalogName() : platform.getDefaultCatalog();
         String schemaName = history.getSourceSchemaName() != null ? history.getSourceSchemaName() : platform.getDefaultSchema();
         if (Table.getFullyQualifiedTableName(catalogName, schemaName, history.getSourceTableName()).equalsIgnoreCase(tableName)) {
            triggerId = history.getTriggerId();
            break;
         }
      }

      return triggerId != null ? this.a.getTriggerRouterService().getTriggerById(triggerId) : null;
   }

   @Override
   public String b() {
      return "channelsForeignKey";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
