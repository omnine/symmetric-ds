package com.jumpmind.symmetric.console.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.jumpmind.symmetric.console.impl.*;
//import com.jumpmind.symmetric.console.impl.LicenseExpireMonitor;
//import com.jumpmind.symmetric.console.impl.LicenseRowsMonitor;


//import com.jumpmind.symmetric.console.impl.ChannelsForeignKeyMonitor;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.model.Notification;
import com.jumpmind.symmetric.console.service.IBackgroundNoHangupService;
import com.jumpmind.symmetric.console.service.IMonitorService;
import com.jumpmind.symmetric.console.ui.common.Helper;
import com.jumpmind.symmetric.notification.INotificationExtension;
import com.jumpmind.symmetric.notification.EmailNotification;
import com.jumpmind.symmetric.notification.LoggerNotification;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.db.sql.ISqlRowMapper;
import org.jumpmind.db.sql.Row;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.db.ISymmetricDialect;
import org.jumpmind.symmetric.ext.ICached;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.symmetric.service.IClusterService;
import org.jumpmind.symmetric.service.IContextService;
import org.jumpmind.symmetric.service.IExtensionService;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.service.impl.AbstractService;
import org.jumpmind.util.AppUtils;
import org.jumpmind.util.LogSummary;

public class MonitorService extends AbstractService implements IMonitorService, ICached, ISymmetricEngineAware {
   protected String hostName;
   protected INodeService nodeService;
   protected IExtensionService extensionService;
   protected IClusterService clusterService;
   protected IContextService contextService;
   protected IBackgroundNoHangupService backgroundNoHangupService;
   protected Map<String, Long> checkTimesByType = new HashMap<>();
   protected Map<String, List<Long>> averagesByType = new HashMap<>();
   protected String typeColumnName;
   protected Set<String> invalidMonitorTypes = new HashSet<>();
   private com.jumpmind.symmetric.cache.IProCacheManager proCacheManager;

   public MonitorService() {
   }

   public MonitorService(ISymmetricEngine engine, ISymmetricDialect symmetricDialect) {
      super(engine.getParameterService(), symmetricDialect);
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.symmetricDialect = engine.getSymmetricDialect();
      this.parameterService = engine.getParameterService();
      this.nodeService = engine.getNodeService();
      this.extensionService = engine.getExtensionService();
      this.clusterService = engine.getClusterService();
      this.contextService = engine.getContextService();
      this.backgroundNoHangupService = (IBackgroundNoHangupService)this.extensionService.getExtensionPoint(IBackgroundNoHangupService.class);
      this.tablePrefix = this.parameterService.getTablePrefix();
      this.platform = this.symmetricDialect.getPlatform();
      this.sqlTemplate = this.symmetricDialect.getPlatform().getSqlTemplate();
      this.sqlTemplateDirty = this.symmetricDialect.getPlatform().getSqlTemplateDirty();
      MonitorServiceSqlMap sqlMap = new MonitorServiceSqlMap(this.symmetricDialect.getPlatform(), this.createSqlReplacementTokens());
      this.typeColumnName = sqlMap.getTypeColumnName();
      this.setSqlMap(sqlMap);
      this.hostName = StringUtils.left(AppUtils.getHostName(), 60);
      MonitorExtension[] monitorExtensions = new MonitorExtension[]{
         new BatchErrorMonitor(),
         new BatchUnsentMonitor(),
         new BatchUnsentOfflineMonitor(),
         new CPUMonitor(),
         new DataGapMonitor(),
         new DiskMonitor(),
         new MemoryMonitor(),
         new DataUnroutedMonitor(),
         new LogMonitor(),
         new OfflineNodesMonitor(),
         new LoadAverageMonitor(),
         new FileHandlesMonitor(),
         new JobErrorMonitor(),
//         new LicenseExpireMonitor(),
         new CertExpireMonitor(),
//         new LicenseRowsMonitor(),
         new JVM64BitMonitor(),
         new JVMCrashMonitor(),
         new JVMOutOfMemoryMonitor(),
         new JVMThreadsMonitor(),
         new BlockMonitor(),
         new MySqlModeMonitor(),
         new NextDataInGapMonitor(),
         new ChannelsDisabledMonitor(),
         new MaxBatchSizeMonitor(),
         new MaxDataToRouteMonitor(),
         new MaxBatchToSendMonitor(),
         new MaxChannelsMonitor(),
         new ChannelSuspendMonitor(),
         new MissingPrimaryKeyMonitor(),
//         new ChannelsForeignKeyMonitor(),
         new JobTrendingMonitor(),
         new ConnectionPoolMonitor(),
         new ConnectionResetMonitor(),
         new LOBMonitor(),
         new StrandedOrExpiredDataMonitor(),
         new UnknownCaMonitor(),
      };

      for (MonitorExtension ext : monitorExtensions) {
         this.extensionService.addExtensionPoint(ext.b(), ext);
      }

      INotificationExtension[] notificationExtensions = new INotificationExtension[]{new LoggerNotification(), new EmailNotification()};

      for (INotificationExtension ext : notificationExtensions) {
         this.extensionService.addExtensionPoint(ext.a(), ext);
      }
   }

   @Override
   public synchronized void update() {
      Map<String, MonitorExtension> monitorTypes = this.extensionService.getExtensionPointMap(MonitorExtension.class);
      Node identity = this.nodeService.findIdentity();
      if (identity != null) {
         List<Monitor> activeMonitors = this.getActiveMonitorsForNode(identity.getNodeGroupId(), identity.getExternalId());
         Map<String, MonitorEvent> unresolved = this.getMonitorEventsNotResolvedForNode(identity.getNodeId());
         Map<String, List<MonitorEvent>> resolved = this.getMonitorEventsResolvedForNode(identity.getNodeId());

         for (Monitor monitor : activeMonitors) {
            MonitorExtension monitorType = monitorTypes.get(monitor.getType());
            if (monitorType != null) {
               if (!monitorType.a()) {
                  boolean processedInsight = false;
                  if (monitorType instanceof InsightMonitor) {
                     processedInsight = this.processInsight(monitor.getMonitorId(), (InsightMonitor)monitorType, unresolved, resolved);
                  }

                  if (!processedInsight) {
                     Long lastCheckTimeLong = this.checkTimesByType.get(monitor.getMonitorId());
                     long lastCheckTime = lastCheckTimeLong != null ? lastCheckTimeLong : 0L;
                     if (lastCheckTime == 0L || (System.currentTimeMillis() - lastCheckTime) / 1000L >= (long)monitor.getRunPeriod()) {
                        this.checkTimesByType.put(monitor.getMonitorId(), System.currentTimeMillis());
                        this.updateMonitor(monitor, monitorType, identity, unresolved);
                     }
                  }
               }
            } else if (this.invalidMonitorTypes.add(monitor.getType())) {
               this.log.warn("Could not find monitor of type '" + monitor.getType() + "'");
            }
         }

         if (this.clusterService.lock("Monitor")) {
            try {
               Gson gson = new Gson();
               Type mapType = (new TypeToken<Map<String, Long>>() {
               }).getType();
               String json = this.contextService.getString("monitor.last.check.times");
               Map<String, Long> clusteredCheckTimesByType = new HashMap<>();
               if (json != null && json.length() > 0) {
                  clusteredCheckTimesByType = (Map<String, Long>)gson.fromJson(json, mapType);
               }

               for (Monitor monitorx : activeMonitors) {
                  MonitorExtension monitorType = monitorTypes.get(monitorx.getType());
                  if (monitorType != null && monitorType.a()) {
                     boolean processedInsightx = false;
                     if (monitorType instanceof InsightMonitor) {
                        processedInsightx = this.processInsight(monitorx.getMonitorId(), (InsightMonitor)monitorType, unresolved, resolved);
                     }

                     if (!processedInsightx) {
                        Long lastCheckTimeLong = clusteredCheckTimesByType.get(monitorx.getMonitorId());
                        long lastCheckTime = lastCheckTimeLong != null ? lastCheckTimeLong : 0L;
                        if (lastCheckTime == 0L || (System.currentTimeMillis() - lastCheckTime) / 1000L >= (long)monitorx.getRunPeriod()) {
                           clusteredCheckTimesByType.put(monitorx.getMonitorId(), System.currentTimeMillis());
                           this.updateMonitor(monitorx, monitorType, identity, unresolved);
                        }
                     }
                  }
               }

               json = gson.toJson(clusteredCheckTimesByType, mapType);
               this.contextService.save("monitor.last.check.times", json);
               int minSeverityLevel = Integer.MAX_VALUE;
               List<Notification> notifications = this.getActiveNotificationsForNode(identity.getNodeGroupId(), identity.getExternalId());
               if (notifications.size() > 0) {
                  for (Notification notification : notifications) {
                     if (notification.getSeverityLevel() < minSeverityLevel) {
                        minSeverityLevel = notification.getSeverityLevel();
                     }
                  }

                  Map<String, INotificationExtension> notificationTypes = this.extensionService.getExtensionPointMap(INotificationExtension.class);
                  List<MonitorEvent> allMonitorEvents = this.getMonitorEventsForNotification(minSeverityLevel);

                  for (Notification notificationx : notifications) {
                     List<MonitorEvent> monitorEvents = new ArrayList<>();

                     for (MonitorEvent monitorEvent : allMonitorEvents) {
                        if (monitorEvent.getSeverityLevel() >= notificationx.getSeverityLevel()) {
                           monitorEvents.add(monitorEvent);
                        }
                     }

                     if (monitorEvents.size() > 0) {
                        INotificationExtension notificationType = notificationTypes.get(notificationx.getType());
                        if (notificationType != null) {
                           notificationType.a(notificationx, monitorEvents);
                           this.updateMonitorEventAsNotified(monitorEvents);
                        } else {
                           this.log.warn("Could not find notification of type '" + notificationx.getType() + "'");
                        }
                     }
                  }
               }
            } finally {
               this.clusterService.unlock("Monitor");
            }
         }
      }
   }

   protected void updateMonitor(Monitor monitor, MonitorExtension monitorType, Node identity, Map<String, MonitorEvent> unresolved) {
      MonitorEvent eventValue = monitorType.a(monitor);
      boolean readyToCompare = true;
      if (!monitorType.a() && monitor.getRunCount() > 0) {
         List<Long> averages = this.averagesByType.get(monitor.getType());
         if (averages == null) {
            averages = new ArrayList<>();
            this.averagesByType.put(monitor.getType(), averages);
         }

         averages.add(eventValue.getValue());

         while (averages.size() > monitor.getRunCount()) {
            averages.remove(0);
         }

         if (averages.size() == monitor.getRunCount()) {
            long accumValue = 0L;

            for (Long oneValue : averages) {
               accumValue += oneValue;
            }

            eventValue.setValue(accumValue / (long)monitor.getRunCount());
         } else {
            readyToCompare = false;
         }
      }

      if (readyToCompare) {
         MonitorEvent event = unresolved.get(monitor.getMonitorId());
         Date now = new Date(System.currentTimeMillis() / 1000L * 1000L);
         boolean checkAboveThreshold = !monitor.getType().equals("maxDataToRoute");
         if (event == null
            || event.isResolved()
            || (!checkAboveThreshold || eventValue.getValue() >= monitor.getThreshold())
               && (checkAboveThreshold || eventValue.getValue() <= monitor.getThreshold())) {
            if (checkAboveThreshold && eventValue.getValue() >= monitor.getThreshold()
               || !checkAboveThreshold && eventValue.getValue() <= monitor.getThreshold()) {
               if (event != null && !event.isResolved()) {
                  event.setHostName(this.hostName);
                  event.setType(monitor.getType());
                  if (monitor.getType().equals("batchError")
                     && monitor.getExpression() != null
                     && monitor.getExpression().equals("notifyOnIncrease=true")
                     && eventValue.getValue() > event.getValue()) {
                     event.setNotified(false);
                  }

                  event.setValue(eventValue.getValue());
                  if (eventValue.getCount() == 0) {
                     event.setCount(event.getCount() + 1);
                  } else {
                     event.setCount(eventValue.getCount());
                  }

                  event.setThreshold(monitor.getThreshold());
                  event.setSeverityLevel(monitor.getSeverityLevel());
                  event.setLastUpdateTime(now);
                  event.setDetails(eventValue.getDetails());
                  event.setInsight(monitor.isInsight());
               } else {
                  if (monitor.getType().equals("log") && this.isLogMonitorEventResolved(monitor, eventValue, identity.getNodeId())) {
                     return;
                  }

                  event = new MonitorEvent();
                  event.setMonitorId(monitor.getMonitorId());
                  event.setNodeId(identity.getNodeId());
                  event.setEventTime(now);
                  event.setHostName(this.hostName);
                  event.setType(monitor.getType());
                  event.setValue(eventValue.getValue());
                  if (eventValue.getCount() == 0) {
                     event.setCount(1);
                  } else {
                     event.setCount(eventValue.getCount());
                  }

                  event.setThreshold(monitor.getThreshold());
                  event.setSeverityLevel(monitor.getSeverityLevel());
                  event.setLastUpdateTime(now);
                  event.setDetails(eventValue.getDetails());
                  event.setInsight(monitor.isInsight());
               }

               this.saveMonitorEvent(event);
            }
         } else {
            event.setLastUpdateTime(now);
            this.updateMonitorEventAsResolved(event);
         }
      }
   }

   private boolean isLogMonitorEventResolved(Monitor monitor, MonitorEvent eventValue, String nodeId) {
      Gson gson = Helper.getMonitorEventGson();
      List<LogSummary> eventLogSummaries = (List<LogSummary>)gson.fromJson(eventValue.getDetails(), (new TypeToken<List<LogSummary>>() {
      }).getType());
      if (eventLogSummaries == null) {
         return false;
      } else {
         List<MonitorEvent> resolvedEvents = this.getMonitorEventsResolvedForNode(nodeId).get(monitor.getMonitorId());

         for (LogSummary eventLogSummary : eventLogSummaries) {
            boolean logMessageResolved = false;

            for (MonitorEvent resolvedEvent : resolvedEvents) {
               if (resolvedEvent.getLastUpdateTime() != null && eventLogSummary.getMostRecentTime() <= resolvedEvent.getLastUpdateTime().getTime()) {
                  for (LogSummary resolvedLogSummary : (List<LogSummary>)gson.fromJson(resolvedEvent.getDetails(), (new TypeToken<List<LogSummary>>() {
                  }).getType())) {
                     if (eventLogSummary.getMessage() != null && eventLogSummary.getMessage().equals(resolvedLogSummary.getMessage())) {
                        logMessageResolved = true;
                        break;
                     }
                  }

                  if (logMessageResolved) {
                     break;
                  }
               }
            }

            if (!logMessageResolved) {
               return false;
            }
         }

         return true;
      }
   }


   private boolean processInsight(String insightId, InsightMonitor insightType, Map<String, MonitorEvent> unresolved, Map<String, List<MonitorEvent>> resolved) {
      boolean processed = false;
      MonitorEvent event = unresolved.get(insightId);
      if (event != null && event.getApprovedOption() > 0 && !event.isApprovalProcessed()) {
         this.processApproval(insightType, event);
         unresolved.remove(insightId);
         processed = true;
      }

      List<MonitorEvent> resolvedList = resolved.get(insightId);
      if (resolvedList != null) {
         for (MonitorEvent resolvedEvent : resolvedList) {
            if (resolvedEvent.getApprovedOption() > 0 && !resolvedEvent.isApprovalProcessed()) {
               this.processApproval(insightType, resolvedEvent);
            }
         }
      }

      return processed;
   }

   private void processApproval(final InsightMonitor insightType, final MonitorEvent event) {
      event.setApprovalProcessed(true);
      this.saveMonitorEvent(event);
      event.setLastUpdateTime(new Date(System.currentTimeMillis() / 1000L * 1000L));
      this.updateMonitorEventAsResolved(event);
      this.backgroundNoHangupService.queueWork(new IRefresh<Void>() {
         public Void onBackgroundDataRefresh(ISymmetricEngine engine) {
            if (!insightType.a(event, deserializeRecommendation(event))) {
               for (MonitorEvent existingEvent : MonitorService.this.getMonitorEventsByMonitorId(event.getMonitorId())) {
                  if (existingEvent.getNodeId().equals(event.getNodeId()) && existingEvent.getEventTime().equals(event.getEventTime())) {
                     existingEvent.setApprovalProcessed(false);
                     MonitorService.this.saveMonitorEvent(existingEvent);
                     existingEvent.setLastUpdateTime(new Date(System.currentTimeMillis() / 1000L * 1000L));
                     MonitorService.this.updateMonitorEventAsUnresolved(existingEvent);
                     break;
                  }
               }
            }

            return null;
         }

         public void onBackgroundUIRefresh(Void backgroundData) {
         }

         @Override
         public void onUIError(Throwable ex) {
         }
      }, null);
   }

   @Override
   public List<Monitor> getMonitors() {
      return this.sqlTemplate.query(this.getSql(new String[]{"selectMonitorSql"}), new MonitorService.MonitorRowMapper(), new Object[0]);
   }

   @Override
   public List<Monitor> getActiveMonitorsForNode(String nodeGroupId, String externalId) {
      return this.getProCacheManager().getActiveMonitorsForNode(nodeGroupId, externalId);
   }

   @Override
   public List<Monitor> getActiveMonitorsForNodeFromDb(String nodeGroupId, String externalId) {
      return this.sqlTemplate
         .query(
            this.getSql(new String[]{"selectMonitorSql", "whereMonitorByNodeSql"}),
            new MonitorService.MonitorRowMapper(),
            new Object[]{nodeGroupId, externalId}
         );
   }

   @Override
   public List<Monitor> getActiveMonitorsUnresolvedForNode(String nodeGroupId, String externalId) {
      return this.getProCacheManager().getActiveMonitorsUnresolvedForNode(nodeGroupId, externalId);
   }

   @Override
   public List<Monitor> getActiveMonitorsUnresolvedForNodeFromDb(String nodeGroupId, String externalId) {
      return this.sqlTemplate
         .query(this.getSql(new String[]{"selectMonitorWhereNotResolved"}), new MonitorService.MonitorRowMapper(), new Object[]{nodeGroupId, externalId});
   }

   @Override
   public void deleteMonitor(String monitorId) {
      this.sqlTemplate.update(this.getSql(new String[]{"deleteMonitorSql"}), new Object[]{monitorId});
   }

   @Override
   public void saveMonitor(Monitor monitor) {
      int count = this.sqlTemplate
         .update(
            this.getSql(new String[]{"updateMonitorSql"}),
            new Object[]{
               monitor.getExternalId(),
               monitor.getNodeGroupId(),
               monitor.getType(),
               monitor.getExpression(),
               monitor.isEnabled() ? 1 : 0,
               monitor.getThreshold(),
               monitor.getRunPeriod(),
               monitor.getRunCount(),
               monitor.getSeverityLevel(),
               monitor.getDisplayOrder(),
               monitor.isInsight() ? 1 : 0,
               monitor.isPinned() ? 1 : 0,
               monitor.getLastUpdateBy(),
               monitor.getLastUpdateTime(),
               monitor.getMonitorId()
            }
         );
      if (count == 0) {
         this.sqlTemplate
            .update(
               this.getSql(new String[]{"insertMonitorSql"}),
               new Object[]{
                  monitor.getMonitorId(),
                  monitor.getExternalId(),
                  monitor.getNodeGroupId(),
                  monitor.getType(),
                  monitor.getExpression(),
                  monitor.isEnabled() ? 1 : 0,
                  monitor.getThreshold(),
                  monitor.getRunPeriod(),
                  monitor.getRunCount(),
                  monitor.getSeverityLevel(),
                  monitor.getDisplayOrder(),
                  monitor.isInsight() ? 1 : 0,
                  monitor.isPinned() ? 1 : 0,
                  monitor.getCreateTime(),
                  monitor.getLastUpdateBy(),
                  monitor.getLastUpdateTime()
               }
            );
      }
   }

   @Override
   public void saveMonitorAsCopy(Monitor monitor) {
      String newId = monitor.getMonitorId();
      List<Monitor> monitors = this.sqlTemplate
         .query(this.getSql(new String[]{"selectMonitorSql", "whereMonitorIdLikeSql"}), new MonitorService.MonitorRowMapper(), new Object[]{newId + "%"});
      List<String> ids = monitors.stream().map(Monitor::getMonitorId).collect(Collectors.toList());
      String suffix = "";

      for (int i = 2; ids.contains(newId + suffix); i++) {
         suffix = "_" + i;
      }

      monitor.setMonitorId(newId + suffix);
      this.saveMonitor(monitor);
   }

   @Override
   public void renameMonitor(String oldId, Monitor monitor) {
      this.deleteMonitor(oldId);
      this.saveMonitor(monitor);
   }

   @Override
   public List<MonitorEvent> getMonitorEvents() {
      return this.sqlTemplateDirty.query(this.getSql(new String[]{"selectMonitorEventSql"}), new MonitorService.MonitorEventRowMapper(), new Object[0]);
   }

   protected Map<String, List<MonitorEvent>> getMonitorEventsResolvedForNode(String nodeId) {
      List<MonitorEvent> list = this.sqlTemplateDirty
         .query(
            this.getSql(new String[]{"selectMonitorEventSql", "whereMonitorEventResolvedSql"}),
            new MonitorService.MonitorEventRowMapper(),
            new Object[]{nodeId}
         );
      Map<String, List<MonitorEvent>> map = new HashMap<>();

      for (MonitorEvent monitorEvent : list) {
         String monitorId = monitorEvent.getMonitorId();
         List<MonitorEvent> listByMonitorId = map.get(monitorId);
         if (listByMonitorId == null) {
            listByMonitorId = new ArrayList<>();
         }

         listByMonitorId.add(monitorEvent);
         map.put(monitorId, listByMonitorId);
      }

      return map;
   }

   protected Map<String, MonitorEvent> getMonitorEventsNotResolvedForNode(String nodeId) {
      List<MonitorEvent> list = this.sqlTemplateDirty
         .query(
            this.getSql(new String[]{"selectMonitorEventSql", "whereMonitorEventNotResolvedSql"}),
            new MonitorService.MonitorEventRowMapper(),
            new Object[]{nodeId}
         );
      Map<String, MonitorEvent> map = new HashMap<>();

      for (MonitorEvent monitorEvent : list) {
         map.put(monitorEvent.getMonitorId(), monitorEvent);
      }

      return map;
   }

   @Override
   public List<MonitorEvent> getMonitorEventsFiltered(int limit, String type, int severityLevel, String nodeId, Boolean isResolved) {
      String sql = this.getSql(new String[]{"selectMonitorEventSql", "whereMonitorEventFilteredSql"});
      ArrayList<Object> args = new ArrayList<>();
      args.add(severityLevel);
      if (isResolved != null) {
         sql = sql + " and is_resolved = ?";
         args.add(isResolved ? 1 : 0);
      }

      if (type != null) {
         sql = sql + " and " + this.typeColumnName + " = ?";
         args.add(type);
      }

      if (nodeId != null) {
         sql = sql + " and node_id = ?";
         args.add(nodeId);
      }

      sql = sql + " order by event_time desc";
      return this.sqlTemplateDirty.query(sql, limit, new MonitorService.MonitorEventRowMapper(), args.toArray());
   }

   @Override
   public List<MonitorEvent> getMonitorEventsByMonitorId(String monitorId) {
      String sql = this.getSql(new String[]{"selectMonitorEventSql", "whereMonitorEventIdSql"});
      sql = sql + " order by event_time desc";
      return this.sqlTemplateDirty.query(sql, new MonitorService.MonitorEventRowMapper(), new Object[]{monitorId});
   }

   protected List<MonitorEvent> getMonitorEventsForNotification(int severityLevel) {
      return this.sqlTemplateDirty
         .query(
            this.getSql(new String[]{"selectMonitorEventSql", "whereMonitorEventForNotificationBySeveritySql"}),
            new MonitorService.MonitorEventRowMapper(),
            new Object[]{severityLevel}
         );
   }

   @Override
   public void saveMonitorEvent(MonitorEvent event) {
      if (!this.updateMonitorEvent(event)) {
         this.insertMonitorEvent(event);
      }
   }

   protected void insertMonitorEvent(MonitorEvent event) {
      this.sqlTemplate
         .update(
            this.getSql(new String[]{"insertMonitorEventSql"}),
            new Object[]{
               event.getMonitorId(),
               event.getNodeId(),
               event.getEventTime(),
               event.getHostName(),
               event.getType(),
               event.getValue(),
               event.getCount(),
               event.getThreshold(),
               event.getSeverityLevel(),
               event.isResolved() ? 1 : 0,
               event.isNotified() ? 1 : 0,
               event.isInsight() ? 1 : 0,
               event.getNotBefore(),
               event.getApprovedOption(),
               event.getApprovedBy(),
               event.isApprovalProcessed() ? 1 : 0,
               event.getDetails(),
               event.getLastUpdateTime()
            }
         );
   }

   protected boolean updateMonitorEvent(MonitorEvent event) {
      int count = this.sqlTemplate
         .update(
            this.getSql(new String[]{"updateMonitorEventSql"}),
            new Object[]{
               event.getHostName(),
               event.getType(),
               event.getValue(),
               event.getCount(),
               event.getThreshold(),
               event.getSeverityLevel(),
               event.isNotified() ? 1 : 0,
               event.isInsight() ? 1 : 0,
               event.getNotBefore(),
               event.getApprovedOption(),
               event.getApprovedBy(),
               event.isApprovalProcessed() ? 1 : 0,
               event.getLastUpdateTime(),
               event.getDetails(),
               event.getMonitorId(),
               event.getNodeId(),
               event.getEventTime()
            }
         );
      return count != 0;
   }

   @Override
   public void deleteMonitorEvent(MonitorEvent event) {
      this.sqlTemplate.update(this.getSql(new String[]{"deleteMonitorEventSql"}), new Object[]{event.getMonitorId(), event.getNodeId(), event.getEventTime()});
   }

   protected void updateMonitorEventAsNotified(List<MonitorEvent> events) {
      for (MonitorEvent event : events) {
         this.updateMonitorEventAsNotified(event);
      }
   }

   protected void updateMonitorEventAsNotified(MonitorEvent event) {
      this.sqlTemplate
         .update(this.getSql(new String[]{"updateMonitorEventNotifiedSql"}), new Object[]{event.getMonitorId(), event.getNodeId(), event.getEventTime()});
   }

   @Override
   public void updateMonitorEventAsResolved(MonitorEvent event) {
      this.sqlTemplate
         .update(
            this.getSql(new String[]{"updateMonitorEventResolvedSql"}),
            new Object[]{event.getLastUpdateTime(), event.getMonitorId(), event.getNodeId(), event.getEventTime()}
         );
   }

   public void updateMonitorEventAsUnresolved(MonitorEvent event) {
      this.sqlTemplate
         .update(
            this.getSql(new String[]{"updateMonitorEventUnresolvedSql"}),
            new Object[]{event.getLastUpdateTime(), event.getMonitorId(), event.getNodeId(), event.getEventTime()}
         );
   }
/*
   @Override
   public Recommendations getRecommendations(boolean dismissed) {
      Date now = new Date();
      List<Monitor> monitorList = this.getMonitors();
      Map<String, MonitorExtension> monitorTypes = this.extensionService.getExtensionPointMap(MonitorExtension.class);
      Recommendations recommendations = new Recommendations();

      for (MonitorEvent event : this.getMonitorEvents()) {
         Date notBefore = event.getNotBefore();
         if (!event.isResolved() && (dismissed && notBefore != null && notBefore.after(now) || !dismissed && (notBefore == null || !notBefore.after(now)))) {
            MonitorExtension monitorType = monitorTypes.get(event.getType());
            if (monitorType instanceof InsightMonitor) {
               for (Monitor monitor : monitorList) {
                  if (monitor.getMonitorId().equals(event.getMonitorId())) {
                     if (monitor.isInsight()) {
                        recommendations.a(this.deserializeRecommendation(event));
                     }
                     break;
                  }
               }
            }
         }
      }

      return recommendations;
   }










*/
   @Override
   public void undoDismissalForRecommendation(String monitorId, String nodeId, Date eventTime) {
      for (MonitorEvent event : this.getMonitorEventsByMonitorId(monitorId)) {
         if (event.getNodeId().equals(nodeId) && event.getEventTime().equals(eventTime)) {
            event.setNotBefore(null);
            event.setLastUpdateTime(new Date(System.currentTimeMillis() / 1000L * 1000L));
            this.saveMonitorEvent(event);
            return;
         }
      }
   }

   @Override
   public void dismissRecommendation(String monitorId, String nodeId, Date eventTime, long duration) {
      for (MonitorEvent event : this.getMonitorEventsByMonitorId(monitorId)) {
         if (event.getNodeId().equals(nodeId) && event.getEventTime().equals(eventTime)) {
            long currentTimeMillis = System.currentTimeMillis() / 1000L * 1000L;
            if (duration > 0L) {
               event.setNotBefore(new Date(currentTimeMillis + duration));
            } else {
               event.setNotBefore(new Date(2147483647000L));
            }

            event.setLastUpdateTime(new Date(currentTimeMillis));
            this.saveMonitorEvent(event);
            return;
         }
      }
   }

   @Override
   public void approveRecommendation(String monitorId, String nodeId, Date eventTime, int optionId, String userId) {
      for (MonitorEvent event : this.getMonitorEventsByMonitorId(monitorId)) {
         if (event.getNodeId().equals(nodeId) && event.getEventTime().equals(eventTime)) {
            event.setApprovedOption(optionId);
            event.setApprovedBy(userId);
            event.setApprovalProcessed(false);
            event.setLastUpdateTime(new Date(System.currentTimeMillis() / 1000L * 1000L));
            this.saveMonitorEvent(event);
            return;
         }
      }
   }

   @Override
   public Map<String, Object> getRecommendationDetails(String monitorId, String nodeId, Date eventTime) {
      for (MonitorEvent event : this.getMonitorEventsByMonitorId(monitorId)) {
         if (event.getNodeId().equals(nodeId) && event.getEventTime().equals(eventTime)) {
            Recommendation recommendation = this.deserializeRecommendation(event);
            if (recommendation != null) {
               return recommendation.c();
            }
         }
      }

      return null;
   }

   private Recommendation deserializeRecommendation(MonitorEvent event) {
   try {
      Gson gson =Helper.getMonitorEventGson();
      Recommendation recommendation = (Recommendation)gson.fromJson(event.getDetails(), Recommendation.class);
      recommendation.d(event.getMonitorId());
      recommendation.e(event.getType());
      recommendation.a(event.getNodeId(), event.getEventTime());
      recommendation.c(event.getSeverityLevel());
      return recommendation;
   } catch (Exception var4) {
      this.log.error("Failed to convert monitor event details from JSON to recommendation", var4);
      return null;
   }
}

   @Override
   public List<Notification> getNotifications() {
      return this.sqlTemplate.query(this.getSql(new String[]{"selectNotificationSql"}), new MonitorService.NotificationRowMapper(), new Object[0]);
   }

   @Override
   public List<Notification> getActiveNotificationsForNode(String nodeGroupId, String externalId) {
      return this.getProCacheManager().getActiveNotificationsForNode(nodeGroupId, externalId);
   }

   @Override
   public List<Notification> getActiveNotificationsForNodeFromDb(String nodeGroupId, String externalId) {
      return this.sqlTemplate
         .query(
            this.getSql(new String[]{"selectNotificationSql", "whereNotificationByNodeSql"}),
            new MonitorService.NotificationRowMapper(),
            new Object[]{nodeGroupId, externalId}
         );
   }

   @Override
   public void saveNotification(Notification notification) {
      int count = this.sqlTemplate
         .update(
            this.getSql(new String[]{"updateNotificationSql"}),
            new Object[]{
               notification.getNodeGroupId(),
               notification.getExternalId(),
               notification.getSeverityLevel(),
               notification.getType(),
               notification.getExpression(),
               notification.isEnabled() ? 1 : 0,
               notification.getCreateTime(),
               notification.getLastUpdateBy(),
               notification.getLastUpdateTime(),
               notification.getNotificationId()
            }
         );
      if (count == 0) {
         this.sqlTemplate
            .update(
               this.getSql(new String[]{"insertNotificationSql"}),
               new Object[]{
                  notification.getNotificationId(),
                  notification.getNodeGroupId(),
                  notification.getExternalId(),
                  notification.getSeverityLevel(),
                  notification.getType(),
                  notification.getExpression(),
                  notification.isEnabled() ? 1 : 0,
                  notification.getCreateTime(),
                  notification.getLastUpdateBy(),
                  notification.getLastUpdateTime()
               }
            );
      }
   }

   @Override
   public void saveNotificationAsCopy(Notification notification) {
      String newId = notification.getNotificationId();
      List<Notification> notifications = this.sqlTemplate
         .query(
            this.getSql(new String[]{"selectNotificationSql", "whereNotificationIdLikeSql"}),
            new MonitorService.NotificationRowMapper(),
            new Object[]{newId + "%"}
         );
      List<String> ids = notifications.stream().map(Notification::getNotificationId).collect(Collectors.toList());
      String suffix = "";

      for (int i = 2; ids.contains(newId + suffix); i++) {
         suffix = "_" + i;
      }

      notification.setNotificationId(newId + suffix);
      this.saveNotification(notification);
   }

   @Override
   public void renameNotification(String oldId, Notification notification) {
      this.deleteNotification(oldId);
      this.saveNotification(notification);
   }

   @Override
   public void deleteNotification(String notificationId) {
      this.sqlTemplate.update(this.getSql(new String[]{"deleteNotificationSql"}), new Object[]{notificationId});
   }

   @Override
   public void flushMonitorCache() {
      this.getProCacheManager().flushMonitorCache();
   }

   @Override
   public void flushNotificationCache() {
      this.getProCacheManager().flushNotificationCache();
   }

   public void flushCache() {
      this.flushMonitorCache();
      this.flushNotificationCache();
   }

   private com.jumpmind.symmetric.cache.IProCacheManager getProCacheManager() {
      if (this.proCacheManager == null) {
         this.proCacheManager = (com.jumpmind.symmetric.cache.IProCacheManager)this.extensionService.getExtensionPoint(com.jumpmind.symmetric.cache.IProCacheManager.class);
      }

      return this.proCacheManager;
   }

   public static class MonitorEventRowMapper implements ISqlRowMapper<MonitorEvent> {
      public MonitorEvent mapRow(Row row) {
         MonitorEvent m = new MonitorEvent();
         m.setMonitorId(row.getString("monitor_id"));
         m.setNodeId(row.getString("node_id"));
         m.setEventTime(row.getDateTime("event_time"));
         m.setHostName(row.getString("host_name"));
         m.setType(row.getString("type"));
         m.setThreshold(row.getLong("threshold"));
         m.setValue(row.getLong("event_value"));
         m.setCount(row.getInt("event_count"));
         m.setSeverityLevel(row.getInt("severity_level"));
         m.setResolved(row.getBoolean("is_resolved"));
         m.setNotified(row.getBoolean("is_notified"));
         m.setInsight(row.getBoolean("is_insight"));
         m.setNotBefore(row.getDateTime("not_before"));
         m.setApprovedOption(row.getInt("approved_option"));
         m.setApprovedBy(row.getString("approved_by"));
         m.setApprovalProcessed(row.getBoolean("is_approval_processed"));
         m.setLastUpdateTime(row.getDateTime("last_update_time"));
         m.setDetails(row.getString("details"));
         return m;
      }
   }

   static class MonitorRowMapper implements ISqlRowMapper<Monitor> {
      public Monitor mapRow(Row row) {
         Monitor m = new Monitor();
         m.setMonitorId(row.getString("monitor_id"));
         m.setExternalId(row.getString("external_id"));
         m.setNodeGroupId(row.getString("node_group_id"));
         m.setType(row.getString("type"));
         m.setExpression(row.getString("expression"));
         m.setEnabled(row.getBoolean("enabled"));
         m.setThreshold(row.getLong("threshold"));
         m.setRunPeriod(row.getInt("run_period"));
         m.setRunCount(row.getInt("run_count"));
         m.setSeverityLevel(row.getInt("severity_level"));
         m.setDisplayOrder(row.getInt("display_order"));
         m.setInsight(row.getBoolean("is_insight"));
         m.setPinned(row.getBoolean("is_pinned"));
         m.setCreateTime(row.getDateTime("create_time"));
         m.setLastUpdateBy(row.getString("last_update_by"));
         m.setLastUpdateTime(row.getDateTime("last_update_time"));
         return m;
      }
   }

   static class NotificationRowMapper implements ISqlRowMapper<Notification> {
      public Notification mapRow(Row row) {
         Notification n = new Notification();
         n.setNotificationId(row.getString("notification_id"));
         n.setNodeGroupId(row.getString("node_group_id"));
         n.setExternalId(row.getString("external_id"));
         n.setSeverityLevel(row.getInt("severity_level"));
         n.setType(row.getString("type"));
         n.setExpression(row.getString("expression"));
         n.setEnabled(row.getBoolean("enabled"));
         n.setCreateTime(row.getDateTime("create_time"));
         n.setLastUpdateBy(row.getString("last_update_by"));
         n.setLastUpdateTime(row.getDateTime("last_update_time"));
         return n;
      }
   }
}
