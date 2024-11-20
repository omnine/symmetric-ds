package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jumpmind.db.model.CatalogSchema;
import org.jumpmind.db.model.Column;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.db.ISymmetricDialect;
import org.jumpmind.symmetric.db.db2.Db2SymmetricDialect;
import org.jumpmind.symmetric.db.firebird.FirebirdSymmetricDialect;
import org.jumpmind.symmetric.db.interbase.InterbaseSymmetricDialect;
import org.jumpmind.symmetric.db.mysql.MySqlSymmetricDialect;
import org.jumpmind.symmetric.db.oracle.OracleSymmetricDialect;
import org.jumpmind.symmetric.db.postgresql.PostgreSqlSymmetricDialect;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Channel;
import org.jumpmind.symmetric.model.Trigger;
import org.jumpmind.symmetric.model.TriggerHistory;
import org.jumpmind.symmetric.service.IConfigurationService;
import org.jumpmind.symmetric.service.ITriggerRouterService;
import org.jumpmind.symmetric.util.SnapshotUtil;

public class LOBMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      int maxWidth = 8000;
      ISymmetricDialect dialect = this.a.getTargetDialect();
      if (dialect instanceof PostgreSqlSymmetricDialect) {
         maxWidth = 1000000000;
      } else if (dialect instanceof OracleSymmetricDialect) {
         maxWidth = 4000;
      } else if (dialect instanceof MySqlSymmetricDialect) {
         maxWidth = 65535;
      } else if (dialect instanceof Db2SymmetricDialect) {
         maxWidth = 32767;
      }

      boolean isContainsBigLobSupported = dialect instanceof OracleSymmetricDialect
         || dialect instanceof FirebirdSymmetricDialect
         || dialect instanceof InterbaseSymmetricDialect;
      IConfigurationService configService = this.a.getConfigurationService();
      Map<CatalogSchema, List<Table>> tablesMap = SnapshotUtil.getTablesForCaptureByCatalogSchema(this.a);
      List<TriggerHistory> historyList = this.a.getTriggerRouterService().getActiveTriggerHistories();
      Set<String> wideTableNameSet = new HashSet<>();
      Set<String> wideTriggerIdSet = new HashSet<>();
      Set<String> lobTableNameSet = new HashSet<>();
      Set<String> lobTriggerIdSet = new HashSet<>();

      for (List<Table> tableList : tablesMap.values()) {
         for (Table table : tableList) {
            int width = 0;
            boolean hasLobs = false;

            for (Column column : table.getColumns()) {
               int type = column.getMappedTypeCode();
               if (type == 2005 || type == 2004 || type == -1 || type == -16 || type == -4 || type == 2011 || type == 2009) {
                  hasLobs = true;
               } else if (type == -2 || type == -3) {
                  width += column.getSizeAsInt() * 2;
               } else if (type == 16 || type == -7) {
                  width += 2;
               } else if (type == 92) {
                  width += 12;
               } else if (type == 2013) {
                  width += 18;
               } else if (type == 93) {
                  width += 24;
               } else if (type == 2014) {
                  width += 30;
               } else {
                  width += column.getSizeAsInt() + 1;
               }
            }

            if (width > maxWidth || hasLobs) {
               Trigger trigger = this.a(table.getFullyQualifiedTableName(), historyList);
               if (trigger != null) {
                  if (isContainsBigLobSupported) {
                     Channel channel = configService.getChannel(trigger.getChannelId());
                     if (channel != null && (!trigger.isUseCaptureLobs() || !channel.isContainsBigLob())) {
                        if (width > maxWidth) {
                           wideTableNameSet.add(table.getQualifiedTableName());
                           wideTriggerIdSet.add(trigger.getTriggerId());
                        } else if (!trigger.isUseStreamLobs()) {
                           lobTableNameSet.add(table.getQualifiedTableName());
                           lobTriggerIdSet.add(trigger.getTriggerId());
                        }
                     }
                  } else if (!trigger.isUseCaptureLobs()) {
                     if (width > maxWidth) {
                        wideTableNameSet.add(table.getQualifiedTableName());
                        wideTriggerIdSet.add(trigger.getTriggerId());
                     } else if (!trigger.isUseStreamLobs()) {
                        lobTableNameSet.add(table.getQualifiedTableName());
                        lobTriggerIdSet.add(trigger.getTriggerId());
                     }
                  }
               }
            }
         }
      }

      int wideTableCount = wideTableNameSet.size();
      int lobTableCount = lobTableNameSet.size();
      int tableCount = wideTableCount + lobTableCount;
      MonitorEvent event = new MonitorEvent();
      event.setValue((long)tableCount);
      if (tableCount > 0) {
         String problemDescription = "";
         String optionDescription = "";
         if (wideTableCount > 0) {
            if (wideTableCount == 1) {
               String tableName = wideTableNameSet.iterator().next();
               problemDescription = problemDescription
                  + "Table "
                  + tableName
                  + " could store a row that is wider than "
                  + maxWidth
                  + " bytes, which is the limit for data capture.";
               optionDescription = optionDescription + "Enable \"Capture Row As LOB\" for the " + tableName + " table's trigger";
               if (isContainsBigLobSupported) {
                  optionDescription = optionDescription + " and \"Contains Lob or Wide Row Data\" for its channel";
               }
            } else {
               problemDescription = problemDescription
                  + "The following tables could store rows that are wider than "
                  + maxWidth
                  + " bytes, which is the limit for data capture: "
                  + wideTableNameSet
                  + ".";
               optionDescription = optionDescription + "Enable \"Capture Row As LOB\" for the LOB tables' triggers";
               if (isContainsBigLobSupported) {
                  optionDescription = optionDescription + " and \"Contains Lob or Wide Row Data\" for their channel(s)";
               }
            }

            optionDescription = optionDescription + ". ";
         }

         if (lobTableCount > 0) {
            if (lobTableCount == 1) {
               String tableName = lobTableNameSet.iterator().next();
               if (wideTableCount > 0) {
                  problemDescription = problemDescription
                     + " Table "
                     + lobTableNameSet.iterator().next()
                     + " has large object data that could store a row that is wider than the limit for data capture.";
               } else {
                  problemDescription = problemDescription
                     + "Table "
                     + lobTableNameSet.iterator().next()
                     + " has large object data that could store a row that is wider than "
                     + maxWidth
                     + " bytes, which is the limit for data capture.";
               }

               optionDescription = optionDescription + "Enable \"Stream LOBs\" for the " + tableName + " table's trigger. ";
            } else {
               if (wideTableCount > 0) {
                  problemDescription = problemDescription
                     + " The following tables have large object data that could store rows that are wider than the limit for data capture: "
                     + lobTableNameSet
                     + ".";
               } else {
                  problemDescription = problemDescription
                     + "The following tables have large object data that could store rows that are wider than "
                     + maxWidth
                     + " bytes, which is the limit for data capture: "
                     + lobTableNameSet
                     + ".";
               }

               optionDescription = optionDescription + "Enable \"Stream LOBs\" for the wide tables' triggers. ";
            }
         }

         String actionDescription = "Adjust your configuration to account for wide rows.";
         fT recommendation = new fT(problemDescription, actionDescription, true);
         List<fT.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, optionDescription.trim()));
         recommendation.a(options);
         if (wideTableCount > 0) {
            recommendation.a("wideTriggerIdSet", wideTriggerIdSet);
         }

         if (lobTableCount > 0) {
            recommendation.a("lobTriggerIdSet", lobTriggerIdSet);
         }

         event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
      }

      return event;
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
   public boolean a(MonitorEvent event, fT recommendation) {
      ISymmetricDialect dialect = this.a.getTargetDialect();
      boolean isContainsBigLobSupported = dialect instanceof OracleSymmetricDialect
         || dialect instanceof FirebirdSymmetricDialect
         || dialect instanceof InterbaseSymmetricDialect;
      ITriggerRouterService triggerRouterService = this.a.getTriggerRouterService();
      IConfigurationService configService = this.a.getConfigurationService();
      IConsoleEventService consoleEventService = (IConsoleEventService)this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
      String nodeId = this.a.getNodeId();
      Set<String> modifiedTriggerIdSet = new HashSet<>();
      Collection<String> wideTriggerIdSet = (Collection<String>)recommendation.c("wideTriggerIdSet");
      if (wideTriggerIdSet != null) {
         Set<String> modifiedChannelIdSet = new HashSet<>();

         for (String triggerId : wideTriggerIdSet) {
            Trigger trigger = triggerRouterService.getTriggerById(triggerId);
            if (trigger != null) {
               if (!trigger.isUseCaptureLobs()) {
                  trigger.setUseCaptureLobs(true);
                  triggerRouterService.saveTrigger(trigger);
                  modifiedTriggerIdSet.add(triggerId);
               }

               String channelId = trigger.getChannelId();
               if (isContainsBigLobSupported && !modifiedChannelIdSet.contains(channelId)) {
                  Channel channel = configService.getChannel(channelId);
                  if (channel != null && !channel.isContainsBigLob()) {
                     channel.setContainsBigLob(true);
                     configService.saveChannel(channel, true);
                     modifiedChannelIdSet.add(channelId);
                  }
               }
            }
         }

         if (!modifiedChannelIdSet.isEmpty()) {
            consoleEventService.addEvent(
               new ConsoleEvent(event.getApprovedBy(), "Channel Modified", nodeId, nodeId, null, modifiedChannelIdSet.toString().toString())
            );
         }
      }

      Collection<String> lobTriggerIdSet = (Collection<String>)recommendation.c("lobTriggerIdSet");
      if (lobTriggerIdSet != null) {
         for (String triggerIdx : lobTriggerIdSet) {
            Trigger trigger = triggerRouterService.getTriggerById(triggerIdx);
            if (trigger != null && !trigger.isUseStreamLobs()) {
               trigger.setUseStreamLobs(true);
               triggerRouterService.saveTrigger(trigger);
               modifiedTriggerIdSet.add(triggerIdx);
            }
         }
      }

      if (!modifiedTriggerIdSet.isEmpty()) {
         consoleEventService.addEvent(new ConsoleEvent(event.getApprovedBy(), "Table Trigger Modified", nodeId, nodeId, null, modifiedTriggerIdSet.toString()));
      }

      return true;
   }

   @Override
   public String b() {
      return "lob";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
