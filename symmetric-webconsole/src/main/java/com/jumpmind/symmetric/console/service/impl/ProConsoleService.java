package com.jumpmind.symmetric.console.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/*
import com.jumpmind.symmetric.console.impl.I;
import com.jumpmind.symmetric.console.impl.J;
import com.jumpmind.symmetric.console.impl.M;
import com.jumpmind.symmetric.console.impl.O;
import com.jumpmind.symmetric.console.impl.eP;
import com.jumpmind.symmetric.console.impl.eS;
import com.jumpmind.symmetric.console.impl.eT;
import com.jumpmind.symmetric.console.impl.eV;
import com.jumpmind.symmetric.console.impl.eX;
import com.jumpmind.symmetric.console.impl.fa;
import com.jumpmind.symmetric.console.impl.fb;
*/
import com.jumpmind.symmetric.console.impl.MonitorJob;
import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.MonitorSummary;
import com.jumpmind.symmetric.console.model.NodeGroupSummary;
//import com.jumpmind.symmetric.console.remote.IRemoteStatusService;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import com.jumpmind.symmetric.console.service.IProConsoleService;
/*
import com.jumpmind.symmetric.db.ProBulkDataLoaderFactory;
import com.jumpmind.symmetric.db.ProDataLoaderFactory;
import com.jumpmind.symmetric.stage.a;
import com.jumpmind.symmetric.stage.b;
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.db.sql.ISqlRowMapper;
import org.jumpmind.db.sql.ISqlTransaction;
import org.jumpmind.db.sql.Row;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.extension.IProgressListener;
import org.jumpmind.properties.TypedProperties;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.io.stage.IStagedResource;
import org.jumpmind.symmetric.io.stage.StagingFileLock;
import org.jumpmind.symmetric.io.stage.StagingManager;
import org.jumpmind.symmetric.load.DefaultDataLoaderFactory;
import org.jumpmind.symmetric.load.IDataLoaderFactory;
import org.jumpmind.symmetric.model.Channel;
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.symmetric.model.OutgoingBatch;
import org.jumpmind.symmetric.model.ProcessInfo;
import org.jumpmind.symmetric.model.TableReloadStatus;
import org.jumpmind.symmetric.model.Trigger;
import org.jumpmind.symmetric.model.TriggerRouter;
import org.jumpmind.symmetric.service.IExtensionService;
import org.jumpmind.symmetric.service.IParameterService;
import org.jumpmind.symmetric.service.ITriggerRouterService;
import org.jumpmind.symmetric.service.impl.ISqlMap;
import org.jumpmind.symmetric.util.CounterStat;
import org.jumpmind.symmetric.util.CounterStatComparator;
import org.jumpmind.util.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProConsoleService implements IProConsoleService, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine engine;
   final Logger log = LoggerFactory.getLogger(this.getClass());
   private ISqlMap sql;
   private TypedProperties settings;
   private Map<String, TypedProperties> settingsMap = new HashMap<>();
   private File settingsDirectory;

   protected String getSql(String... keys) {
      return this.sql.getSql(keys);
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.engine = engine;
      this.sql = new ProConsoleServiceSqlMap(engine.getSymmetricDialect().getPlatform(), engine.getTablePrefix());
      IExtensionService extensionService = engine.getExtensionService();
      extensionService.addExtensionPoint(new MonitorJob(engine));
/*
      extensionService.addExtensionPoint(new J());
      extensionService.addExtensionPoint(new I());
      extensionService.addExtensionPoint(new eS());
      extensionService.addExtensionPoint(new eT());
      extensionService.addExtensionPoint(new ExpandedTableResolver());
      extensionService.removeExtensionPoint(new DefaultDataLoaderFactory());
      extensionService.addExtensionPoint(new ProDataLoaderFactory(engine));
      extensionService.getExtensionPointMap(IDataLoaderFactory.class).remove("bulkLoaderFactory");
      extensionService.addExtensionPoint(new ProBulkDataLoaderFactory());
      extensionService.addExtensionPoint(new O());
      extensionService.addExtensionPoint(new M());

      extensionService.addExtensionPoint(new fa(engine));
      extensionService.addExtensionPoint(new fb(engine));
      extensionService.addExtensionPoint(new a());
*/

   }

   @Override
   public void cancelLoad(TableReloadStatus obj) {
      List<ProcessInfo> infos = this.engine.getStatisticManager().getProcessInfos();
      List<Long> batchIds = new ArrayList<>();

      for (ProcessInfo info : infos) {
         if (info.getCurrentLoadId() == (long)obj.getLoadId() && info.getCurrentBatchId() > 0L) {
            batchIds.add(info.getCurrentBatchId());
         }
      }

      this.engine.getInitialLoadService().cancelLoad(obj);
      /*
      IRemoteStatusService remoteStatusService = (IRemoteStatusService)this.engine.getExtensionService().getExtensionPoint(IRemoteStatusService.class);
      if (remoteStatusService != null) {
         for (Long batchId : batchIds) {
            this.log.info("Sending interrupt remotely to " + obj.getTargetNodeId() + " for batch " + batchId);
            remoteStatusService.sendMessage(obj.getTargetNodeId(), "batchstop " + batchId);
         }
      }
       */
   }

   @Override
   public TypedProperties getSettings(String user) {
      return this.loadSettings(user);
   }

   @Override
   public void saveSettings(TypedProperties settings, String user) {
      this.settingsDirectory = new File(System.getProperty("java.io.tmpdir"), user);
      this.settingsDirectory.mkdirs();
      synchronized (this.getClass()) {
         File file = new File(this.settingsDirectory, "console-service-settings.json");
         PrintWriter os = null;

         try {
            os = new PrintWriter(new FileOutputStream(file, false));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            os.write(gson.toJson(settings));
            this.settings = settings;
            this.settingsMap.remove(user);
         } catch (Exception var17) {
            this.log.error(var17.getMessage(), var17);
         } finally {
            try {
               os.close();
            } catch (Exception var16) {
            }
         }
      }
   }

   protected TypedProperties loadSettings(String user) {
      if (this.settingsMap.get(user) != null) {
         return this.settingsMap.get(user);
      } else {
         this.settingsDirectory = new File(System.getProperty("java.io.tmpdir"), user);
         this.settingsDirectory.mkdirs();
         synchronized (this.getClass()) {
            File file = new File(this.settingsDirectory, "console-service-settings.json");
            label77:
            if (file.exists() && file.length() > 0L) {
               InputStreamReader is = null;

               TypedProperties var6;
               try {
                  is = new InputStreamReader(new FileInputStream(file));
                  Gson gson = new GsonBuilder().create();
                  this.settings = (TypedProperties)gson.fromJson(is, TypedProperties.class);
                  this.settingsMap.put(user, this.settings);
                  var6 = this.settings;
               } catch (Exception var18) {
                  this.log.error("Failed to load settings", var18);
                  FileUtils.deleteQuietly(file);
                  break label77;
               } finally {
                  try {
                     is.close();
                  } catch (Exception var17) {
                  }
               }

               return var6;
            }

            this.buildProperties();
            this.settingsMap.put(user, this.settings);
            return this.settings;
         }
      }
   }

   private TypedProperties buildProperties() {
      this.settings = new TypedProperties();
      this.settings.put("pro.console.remind.interval", "86400000");
      this.settings.put("pro.console.last.remind.time", new Date(0L).toString());
      this.settings.put("pro.console.newest.version", "");
      this.settings.put("license.expire.remind.interval", "604800000");
      this.settings.put("license.expire.last.remind.time", new Date(0L).toString());
      this.settings.put("cert.expire.remind.interval", "604800000");
      this.settings.put("cert.expire.last.remind.time", new Date(0L).toString());
      this.settings.put("form.show.advanced.options", false);
      this.settings.put("display.appearance", "auto");
      this.settings.put("display.date.format", "yyyy-MM-dd");
      this.settings.put("display.time.format", "hh:mm:ss aaa");
      this.settings.put("display.time.zone", "auto");
      return this.settings;
   }

   @Override
   public void balanceChannelsForInitialLoad(
      String sourceNodeGroupId,
      String targetNodeGroupId,
      int numChannels,
      String copyFromReloadChannelId,
      boolean splitExtractOfLargeTables,
      String userId,
      IProgressListener listener
   ) {
      this.log
         .info(
            "Balancing tables on group link from \"{}\" to \"{}\" for {} channels based on {} channel",
            new Object[]{sourceNodeGroupId, targetNodeGroupId, numChannels, copyFromReloadChannelId}
         );
      String nodeId = this.engine.getNodeId();
      ((IConsoleEventService)this.engine.getExtensionService().getExtensionPoint(IConsoleEventService.class))
         .addEvent(new ConsoleEvent(userId, "Channel Created", nodeId, nodeId, null, "channel: " + copyFromReloadChannelId + ", num=" + numChannels));
      IDatabasePlatform platform = this.engine.getDatabasePlatform();
      ITriggerRouterService triggerRouterService = this.engine.getTriggerRouterService();
      List<TriggerRouter> triggerRouters = triggerRouterService.getAllTriggerRoutersForReloadForCurrentNode(sourceNodeGroupId, targetNodeGroupId);
      List<CounterStat> stats = new ArrayList<>(triggerRouters.size());
      Map<String, Table> tablesByTriggerId = new HashMap<>();
      Map<String, TriggerRouter> triggerRoutersByTriggerId = new HashMap<>();
      long totalRows = 0L;
      int stepNumber = 1;
      int totalSteps = triggerRouters.size() * 2;
      List<TriggerRouter> enabledTriggerRouters = new ArrayList<>();

      for (TriggerRouter triggerRouter : triggerRouters) {
         Trigger trigger = triggerRouter.getTrigger();
         if (triggerRouter.isEnabled()
            && triggerRouter.getInitialLoadOrder() >= 0
            && (trigger.isSyncOnInsert() || trigger.isSyncOnUpdate() || trigger.isSyncOnDelete())) {
            enabledTriggerRouters.add(triggerRouter);
         }
      }

      this.log.info("Found {} tables to balance across {} channels", enabledTriggerRouters.size(), numChannels);

      for (TriggerRouter triggerRouterx : enabledTriggerRouters) {
         Trigger trigger = triggerRouterx.getTrigger();
         triggerRoutersByTriggerId.put(trigger.getTriggerId(), triggerRouterx);
         String catalog = StringUtils.isNotBlank(trigger.getSourceCatalogName()) ? trigger.getSourceCatalogName() : platform.getDefaultCatalog();
         String schema = StringUtils.isNotBlank(trigger.getSourceSchemaName()) ? trigger.getSourceSchemaName() : platform.getDefaultSchema();
         Table table = platform.getTableFromCache(catalog, schema, trigger.getSourceTableName(), false);
         tablesByTriggerId.put(trigger.getTriggerId(), table);
         long rowCount = platform.getEstimatedRowCount(table);
         totalRows += rowCount;
         stats.add(new CounterStat(triggerRouterx, rowCount));
         if (listener != null) {
            listener.checkpoint(this.engine.getEngineName(), stepNumber++, totalSteps);
         }
      }

      DecimalFormat formatter = new DecimalFormat("#,###");
      this.log.info("Total rows is {}", formatter.format(totalRows));
      stats.sort(new CounterStatComparator(false));
      long rowsPerChannel = totalRows / (long)numChannels;
      int currentChannelNum = 0;
      int currentChannelRows = 0;
      int currentChannelTables = 0;
      Map<String, Channel> channels = this.engine.getConfigurationService().getChannels(true);
      Channel copyFromReloadChannel = channels.get(copyFromReloadChannelId);
      String reloadChannelId = this.createReloadChannel(channels, copyFromReloadChannel, currentChannelNum);
      Iterator<CounterStat> iter = stats.iterator();

      while (iter.hasNext()) {
         CounterStat stat = iter.next();
         long rowCount = stat.getCount();
         TriggerRouter triggerRouterxx = (TriggerRouter)stat.getObject();
         Trigger trigger = triggerRouterxx.getTrigger();
         int channelsToUse = (int)((float)rowCount / (float)totalRows * (float)numChannels);
         if (splitExtractOfLargeTables && platform.supportsSliceTables() && channelsToUse > 1) {
            this.log
               .info(
                  "Splitting table {} with {} rows across {} channels", new Object[]{trigger.getSourceTableName(), formatter.format(rowCount), channelsToUse}
               );

            for (int i = 0; i < channelsToUse; i++) {
               this.log.info("Channel {} has 1 partial table and {} rows", reloadChannelId, formatter.format(rowCount / (long)channelsToUse));
               this.sliceTriggerRouter(triggerRoutersByTriggerId, triggerRouterxx, tablesByTriggerId, reloadChannelId, i, channelsToUse);
               if (i + 1 < channelsToUse) {
                  reloadChannelId = this.createReloadChannel(channels, copyFromReloadChannel, ++currentChannelNum);
               }
            }
         } else if (!trigger.getReloadChannelId().equals(reloadChannelId)) {
            trigger.setReloadChannelId(reloadChannelId);
            triggerRouterService.saveTrigger(trigger);
         }

         if (this.engine.getParameterService().is("auto.create.channels.cleanup", true)) {
            this.cleanupOldTriggerRouters(triggerRoutersByTriggerId, trigger.getTriggerId(), channelsToUse);
         }

         currentChannelRows = (int)((long)currentChannelRows + rowCount);
         currentChannelTables++;
         if (iter.hasNext() && (long)currentChannelRows > rowsPerChannel) {
            this.log.info("Channel {} has {} tables and {} rows", new Object[]{reloadChannelId, currentChannelTables, currentChannelRows});
            currentChannelRows = 0;
            currentChannelTables = 0;
            reloadChannelId = this.createReloadChannel(channels, copyFromReloadChannel, ++currentChannelNum);
         }

         if (listener != null) {
            listener.checkpoint(this.engine.getEngineName(), stepNumber++, totalSteps);
         }
      }

      this.log.info("Channel {} has {} tables and {} rows", new Object[]{reloadChannelId, currentChannelTables, currentChannelRows});
      this.log.info("Done balancing channels");
   }

   protected String createReloadChannel(Map<String, Channel> channels, Channel copyFromReloadChannel, int number) {
      String channelId = copyFromReloadChannel.getChannelId();
      if (number > 0) {
         channelId = channelId + (number + 1);
      }

      Channel channelExisting = channels.get(channelId);
      if (channelExisting != null && channelExisting.getChannelId().equals(channelExisting.getQueue())) {
         this.log.debug("Found existing channel {}", channelId);
      } else {
         Channel channelCopy = copyFromReloadChannel.copy();
         channelCopy.setChannelId(channelId);
         channelCopy.setQueue(channelId);
         this.log.info("Saving channel {}", channelId);
         this.engine.getConfigurationService().saveChannel(channelCopy, true);
      }

      return channelId;
   }

   protected void sliceTriggerRouter(
      Map<String, TriggerRouter> triggerRoutersByTriggerId,
      TriggerRouter triggerRouter,
      Map<String, Table> tablesByTriggerId,
      String reloadChannelId,
      int sliceNum,
      int totalSlices
   ) {
      String triggerId = triggerRouter.getTriggerId();
      if (sliceNum > 0) {
         triggerId = triggerId + (sliceNum + 1);
      }

      Table table = tablesByTriggerId.get(triggerRouter.getTriggerId());
      String columnName = null;
      if (table.getPrimaryKeyColumnCount() > 0) {
         columnName = table.getPrimaryKeyColumnNames()[0];
      } else {
         columnName = table.getColumnNames()[0];
      }

      String initialLoadSelect = this.engine.getDatabasePlatform().getSliceTableSql(columnName, sliceNum, totalSlices);
      TriggerRouter triggerRouterExisting = triggerRoutersByTriggerId.get(triggerId);
      if (triggerRouterExisting != null && StringUtils.equals(triggerRouterExisting.getInitialLoadSelect(), initialLoadSelect)) {
         this.log.debug("Found existing trigger router {}", triggerId);
      } else {
         TriggerRouter triggerRouterCopy = triggerRouter.copy();
         triggerRouterCopy.setInitialLoadSelect(initialLoadSelect);
         Trigger triggerCopy = triggerRouterCopy.getTrigger();
         triggerCopy.setTriggerId(triggerId);
         if (sliceNum > 0) {
            triggerCopy.setSyncOnInsert(false);
            triggerCopy.setSyncOnUpdate(false);
            triggerCopy.setSyncOnDelete(false);
         }

         triggerCopy.setReloadChannelId(reloadChannelId);
         this.log.info("Saving trigger router {}", triggerId);
         this.engine.getTriggerRouterService().saveTriggerRouter(triggerRouterCopy);
      }
   }

   protected void cleanupOldTriggerRouters(Map<String, TriggerRouter> triggerRoutersByTriggerId, String triggerId, int maxChannels) {
      int i = maxChannels == 0 ? 2 : maxChannels + 1;

      while (true) {
         TriggerRouter triggerRouter = triggerRoutersByTriggerId.get(triggerId + i);
         if (triggerRouter == null) {
            return;
         }

         Trigger trigger = triggerRouter.getTrigger();
         if (!trigger.isSyncOnInsert() && !trigger.isSyncOnUpdate() && !trigger.isSyncOnDelete()) {
            this.log.info("Clean up old trigger router {}", trigger.getTriggerId());
            this.engine.getTriggerRouterService().deleteTriggerRouter(triggerRouter);
            this.engine.getTriggerRouterService().deleteTrigger(trigger);
         }

         i++;
      }
   }

   @Override
   public void ignoreRowForOutgoingBatchByDataId(OutgoingBatch batch, long dataId) {
      long batchId = batch.getBatchId();
      int idType = this.engine.getSymmetricDialect().getSqlTypeForIds();
      this.engine.getSqlTemplate().update(this.getSql("ignoreRowSql"), new Object[]{dataId, batchId}, new int[]{idType, idType});
      StagingManager stagingManager = (StagingManager)this.engine.getStagingManager();
      String paddedBatchId = StringUtils.leftPad(String.valueOf(batchId), 10, '0');
      String filePath = "outgoing/" + batch.getNodeId() + "/" + paddedBatchId;
      IStagedResource resource = stagingManager.find(filePath);
      if (resource != null) {
         stagingManager.removeResourcePath(filePath);
         resource.delete();
      }
   }

   @Override
   public void ignoreRowForOutgoingBatchByRowNumber(OutgoingBatch batch) {
      long batchId = batch.getBatchId();
      StagingManager stagingManager = (StagingManager)this.engine.getStagingManager();
      String paddedBatchId = StringUtils.leftPad(String.valueOf(batchId), 10, '0');
      String filePath = "outgoing/" + batch.getNodeId() + "/" + paddedBatchId;
      IStagedResource resource = stagingManager.find(filePath);
      StagingFileLock lock = this.engine.getDataExtractorService().acquireStagingFileLock(batch);
      BufferedReader reader = resource.getReader();
      String stagingText = "";
      List<String> lines = reader.lines().collect(Collectors.toList());
      int rowCount = 0;

      for (int i = 0; i < lines.size(); i++) {
         String line = lines.get(i);
         boolean isRow = line.startsWith("insert") || line.startsWith("update") || line.startsWith("delete");
         if (isRow) {
            rowCount++;
         }

         if (!isRow || (long)rowCount != batch.getFailedLineNumber()) {
            stagingText = stagingText + line + "\n";
         }
      }

      IParameterService parameterService = this.engine.getParameterService();

      /*
      //b is stage.b
      try {
         if (!(resource instanceof b)
            || !parameterService.is("stream.to.file.encrypt.enabled", false) && !parameterService.is("stream.to.file.compression.enabled", false)) {
            FileWriter writer = new FileWriter(resource.getFile());
            writer.write(stagingText);
            writer.close();
            resource.refreshLastUpdateTime();
         } else {
            BufferedWriter writer = ((b)resource).getWriter(parameterService.getLong("stream.to.file.threshold.bytes"));
            writer.write(stagingText);
         }
      } catch (IOException var16) {
         this.log.error("Failed to write to " + resource.getFile().getName(), var16);
      }
      */
      resource.close();
      lock.releaseLock();
   }

   @Override
   public void saveTableStats(String tableName, String eventType, long extractedRows, long loadedRows, Date startTime, Date endTime) {
      this.log.debug("Saving table stats");
      ISqlTransaction transaction = null;

      try {
         transaction = this.engine.getSqlTemplate().startSqlTransaction();
         int[] types = new int[]{12, 12, 93, 93, -5, -5};
         transaction.prepareAndExecute(this.getSql("insertTableStat"), new Object[]{tableName, eventType, startTime, endTime, loadedRows, extractedRows}, types);
         transaction.commit();
      } catch (Exception var18) {
         if (transaction != null) {
            transaction.rollback();
            transaction.close();
            transaction = null;
         }

         if (!this.engine.getSqlTemplate().isUniqueKeyViolation(var18)) {
            throw var18 instanceof RuntimeException ? (RuntimeException)var18 : new RuntimeException(var18);
         }

         try {
            transaction = this.engine.getSqlTemplate().startSqlTransaction();
            if (this.engine.getDatabasePlatform().supportsParametersInSelect()) {
               int[] typesx = new int[]{-5, -5, 12, 12, 93, 93};
               transaction.prepareAndExecute(
                  this.getSql("updateTableStat"), new Object[]{loadedRows, extractedRows, tableName, eventType, startTime, endTime}, typesx
               );
            } else {
               String sql = this.getSql("updateTableStatNoParamsInSelect");
               sql = FormatUtils.replace("loadedRows", String.valueOf(loadedRows), sql);
               sql = FormatUtils.replace("extractedRows", String.valueOf(extractedRows), sql);
               int[] typesx = new int[]{12, 12, 93, 93};
               transaction.prepareAndExecute(sql, new Object[]{tableName, eventType, startTime, endTime}, typesx);
            }

            transaction.commit();
         } catch (Exception var17) {
            if (transaction != null) {
               transaction.rollback();
            }

            throw var17 instanceof RuntimeException ? (RuntimeException)var17 : new RuntimeException(var17);
         }
      } finally {
         if (transaction != null) {
            transaction.close();
         }
      }
   }

   protected ISymmetricEngine getSymmetricEngine(List<ISymmetricEngine> engines) {
      ISymmetricEngine engine = null;

      for (ISymmetricEngine thisEngine : engines) {
         if (thisEngine.getParameterService().isRegistrationServer()) {
            engine = thisEngine;
            break;
         }
      }

      if (engine == null && engines.size() > 0) {
         engine = engines.get(0);
      }

      return engine;
   }

   @Override
   public long geAveragePurge() {
      return this.engine.getSqlTemplate().queryForLong(this.getSql("getAveragePurge"), new Object[0]);
   }

   @Override
   public List<Node> geSlowestPurgeNodes(int limit) {
      int i = 0;
      List<Node> nodes = new ArrayList<>();

      for (Row rs : this.engine.getSqlTemplate().query(this.getSql("getSlowestPurgeNodes"))) {
         if (i >= limit) {
            break;
         }

         nodes.add(mapNode(rs));
         i++;
      }

      return nodes;
   }

   @Override
   public List<Node> getOldestPurgeNodes(int limit) {
      int i = 0;
      List<Node> nodes = new ArrayList<>();

      for (Row rs : this.engine.getSqlTemplate().query(this.getSql("getOldestPurgeNodes"))) {
         if (i >= limit) {
            break;
         }

         nodes.add(mapNode(rs));
         i++;
      }

      return nodes;
   }

   @Override
   public long geAverageRouting() {
      return this.engine.getSqlTemplate().queryForLong(this.getSql("getAverageRouting"), new Object[0]);
   }

   @Override
   public List<Node> geSlowestRoutingNodes(int limit) {
      int i = 0;
      List<Node> nodes = new ArrayList<>();

      for (Row rs : this.engine.getSqlTemplate().query(this.getSql("getSlowestRoutingNodes"))) {
         if (i >= limit) {
            break;
         }

         nodes.add(mapNode(rs));
         i++;
      }

      return nodes;
   }

   @Override
   public List<Node> getOldestRoutingNodes(int limit) {
      int i = 0;
      List<Node> nodes = new ArrayList<>();

      for (Row rs : this.engine.getSqlTemplate().query(this.getSql("getOldestRoutingNodes"))) {
         if (i >= limit) {
            break;
         }

         nodes.add(mapNode(rs));
         i++;
      }

      return nodes;
   }

   public static Node mapNode(Row rs) {
      Node node = new Node();
      node.setNodeId(rs.getString("node_id"));
      node.setNodeGroupId(rs.getString("node_group_id"));
      node.setExternalId(rs.getString("external_id"));
      node.setSyncEnabled(rs.getBoolean("sync_enabled"));
      node.setSyncUrl(rs.getString("sync_url"));
      node.setSchemaVersion(rs.getString("schema_version"));
      node.setDatabaseType(rs.getString("database_type"));
      node.setDatabaseVersion(rs.getString("database_version"));
      node.setDatabaseName(rs.getString("database_name"));
      node.setSymmetricVersion(rs.getString("symmetric_version"));
      node.setCreatedAtNodeId(rs.getString("created_at_node_id"));
      node.setBatchToSendCount(rs.getInt("batch_to_send_count"));
      node.setBatchInErrorCount(rs.getInt("batch_in_error_count"));
      node.setDeploymentType(rs.getString("deployment_type"));
      node.setDeploymentSubType(rs.getString("deployment_sub_type"));
      node.setConfigVersion(rs.getString("config_version"));
      node.setPurgeOutgoingAverageMs(rs.getLong("purge_outgoing_average_ms"));
      node.setPurgeOutgoingLastMs(rs.getLong("purge_outgoing_last_run_ms"));
      node.setPurgeOutgoingLastRun(rs.getDateTime("purge_outgoing_last_finish"));
      node.setRoutingAverageMs(rs.getLong("routing_average_run_ms"));
      node.setRoutingLastMs(rs.getLong("routing_last_run_ms"));
      node.setRoutingLastRun(rs.getDateTime("routing_last_finish"));
      node.setSymDataSize(rs.getLong("sym_data_size"));
      node.setMostRecentActiveTableSynced(rs.getString("most_recent_active_table"));
      node.setLastSuccessfulSyncDate(rs.getDateTime("batch_last_successful"));
      node.setDataRowsLoadedCount(rs.getInt("data_rows_loaded_count"));
      node.setDataRowsToSendCount(rs.getInt("data_rows_to_send_count"));
      return node;
   }

   @Override
   public List<MonitorSummary> getMonitorSummary() {
      return this.engine.getSqlTemplate().query(this.getSql("getMonitorSummary"), new ISqlRowMapper<MonitorSummary>() {
         public MonitorSummary mapRow(Row rs) {
            MonitorSummary summary = new MonitorSummary();
            summary.setNodeCount(rs.getInt("node_count"));
            summary.setMonitorId(rs.getString("monitor_id"));
            summary.setSeverityLevel(rs.getInt("severity_level"));
            summary.setMaxEventCount(rs.getInt("max_event_count"));
            summary.setMaxEventTime(rs.getDateTime("max_event_time"));
            summary.setThreshold(rs.getLong("threshold"));
            summary.setType(rs.getString("type"));
            summary.setValue(rs.getLong("event_value"));
            return summary;
         }
      }, new Object[0]);
   }

   @Override
   public List<NodeGroupSummary> getNodeGroupSummary() {
      List<NodeGroupSummary> nodeGroupSummary = new ArrayList<>();

      try {
         nodeGroupSummary = this.engine.getSqlTemplate().query(this.getSql("getNodeGroupSummary"), new ISqlRowMapper<NodeGroupSummary>() {
            public NodeGroupSummary mapRow(Row rs) {
               NodeGroupSummary summary = new NodeGroupSummary();
               summary.setTargetTableCount(rs.getInt("target_table_count"));
               summary.setSourceTableCount(rs.getInt("source_table_count"));
               summary.setNodes(rs.getInt("nodes"));
               summary.setNodeGroupId(rs.getString("node_group_id"));
               summary.setDatabaseType(rs.getString("database_type"));
               return summary;
            }
         }, new Object[0]);
      } catch (Throwable var3) {
         this.log.info("Unable to build node group summary panel on dashboard.", var3.getMessage());
      }

      return nodeGroupSummary;
   }

   @Override
   public List<Node> getNodeSyncSummary(boolean allTiers) {
      return this.engine.getSqlTemplate().query(this.getSql(allTiers ? "getNodeSyncSummary" : "getNodeSyncSummaryChildOnly"), new ISqlRowMapper<Node>() {
         public Node mapRow(Row rs) {
            return ProConsoleService.mapNode(rs);
         }
      }, new Object[0]);
   }

   @Override
   public String getMostRecentActiveTableSynced() {
      Iterator var1 = this.engine.getSqlTemplate().query(this.getSql("getLastTableSynced")).iterator();
      if (var1.hasNext()) {
         Row row = (Row)var1.next();
         return row.getString("table_name");
      } else {
         return null;
      }
   }

   @Override
   public Map<Integer, Date> getTotalLoadedRows() {
      Map<Integer, Date> totals = new HashMap<>();
      Iterator var2 = this.engine.getSqlTemplate().query(this.getSql("getTotalRowsLoaded")).iterator();
      if (var2.hasNext()) {
         Row row = (Row)var2.next();
         totals.put(row.getInt("total_rows"), row.getDateTime("oldest_load_time"));
         return totals;
      } else {
         return totals;
      }
   }
}
