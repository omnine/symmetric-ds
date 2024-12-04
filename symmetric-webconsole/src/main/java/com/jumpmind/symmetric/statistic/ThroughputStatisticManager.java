package com.jumpmind.symmetric.statistic;

import com.jumpmind.symmetric.console.service.IProConsoleService;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.model.Channel;
import org.jumpmind.symmetric.statistic.ChannelStats;
import org.jumpmind.symmetric.statistic.StatisticManager;

public class ThroughputStatisticManager extends StatisticManager implements IThroughputStatisticManager {
   private Map<Date, Map<String, ChannelStats>> channelStatsInMemory = new LinkedHashMap<>();
   private Set<String> reloadChannelsCache = new HashSet<>();
   private long reloadChannelCacheTime;
   private long reloadChannelCacheTimeoutInMs = 900000L;
   private long symmetricRowsIn;
   private long symmetricRowsOut;
   private long symmetricBytesIn;
   private long symmetricBytesOut;
   private long reloadRowsIn;
   private long reloadRowsOut;
   private long reloadBytesIn;
   private long reloadBytesOut;
   private long cdcRowsIn;
   private long cdcRowsOut;
   private long cdcBytesIn;
   private long cdcBytesOut;
   private Map<String, ChannelStats> cdcChannelTotals = new HashMap<>();
   private Map<String, ChannelStats> reloadChannelTotals = new HashMap<>();
   private Map<String, ChannelStats> systemChannelTotals = new HashMap<>();
   private ISymmetricEngine engine;
   private boolean isInitialized;
   private TableStats tableStats;
   IProConsoleService proConsoleService;

   public ThroughputStatisticManager(ISymmetricEngine engine) {
      super(engine.getParameterService(), engine.getNodeService(), engine.getConfigurationService(), engine.getStatisticService(), engine.getClusterService());
      this.engine = engine;
      this.checkInitialized();
      this.tableStats = new TableStats(new Date());
   }

   protected void checkInitialized() {
      if (!this.isInitialized && this.engine.isInitialized() && this.nodeService.findIdentityNodeId() != null) {
         Calendar end = Calendar.getInstance();
         Calendar start = Calendar.getInstance();
         start.add(11, -24);
         this.channelStatsInMemory = this.statisticService
            .getChannelStatsForPeriod(start.getTime(), end.getTime(), this.nodeService.findIdentityNodeId(), 1440);
         this.isInitialized = true;
      }
   }

   protected void saveAdditionalStats(Date endTime, ChannelStats stats) {
      this.checkInitialized();
      if (this.channelStatsInMemory.get(endTime) == null) {
         this.channelStatsInMemory.put(endTime, new HashMap<>());
      }

      ChannelStats incrementalStat = new ChannelStats(stats.getNodeId(), stats.getHostName(), stats.getStartTime(), stats.getEndTime(), stats.getChannelId());
      incrementalStat.add(stats);
      this.channelStatsInMemory.get(endTime).put(incrementalStat.getChannelId(), incrementalStat);
   }

   public void flush() {
      super.flush();

      try {
         this.tableStats.setEndTime(new Date());

         for (Entry<String, Map<String, TableStat>> entry : this.tableStats.getTableCounts().entrySet()) {
            for (Entry<String, TableStat> dmlEntry : entry.getValue().entrySet()) {
               this.getProConsoleService()
                  .saveTableStats(
                     entry.getKey(),
                     dmlEntry.getKey(),
                     dmlEntry.getValue().getExtractedCount(),
                     dmlEntry.getValue().getLoadedCount(),
                     this.tableStats.getStartTime(),
                     this.tableStats.getEndTime()
                  );
            }
         }
      } catch (Exception var8) {
         this.log.info("Unable to save table stats ", var8);
      } finally {
         this.tableStats = new TableStats(new Date());
      }
   }

   @Override
   public void deleteStats(Date start, Date end, String nodeId) {
      this.engine.getStatisticService().deleteChannelStatsForPeriod(start, end, nodeId);
      this.channelStatsInMemory = this.statisticService.getChannelStatsForPeriod(start, end, this.nodeService.findIdentityNodeId(), 1440);
      this.getThroughput(86400000L);
   }

   @Override
   public long getThroughput(Long rangeInMs) {
      this.symmetricRowsIn = 0L;
      this.symmetricRowsOut = 0L;
      this.symmetricBytesIn = 0L;
      this.symmetricBytesOut = 0L;
      this.reloadRowsIn = 0L;
      this.reloadRowsOut = 0L;
      this.reloadBytesIn = 0L;
      this.reloadBytesOut = 0L;
      this.cdcRowsIn = 0L;
      this.cdcRowsOut = 0L;
      this.cdcBytesIn = 0L;
      this.cdcBytesOut = 0L;
      this.cdcChannelTotals.clear();
      this.reloadChannelTotals.clear();
      this.systemChannelTotals.clear();
      this.checkInitialized();
      Date startTime = new Date();
      Date rangeDate = new Date(startTime.getTime() - rangeInMs);

      for (Entry<Date, Map<String, ChannelStats>> entry : this.channelStatsInMemory.entrySet()) {
         for (Entry<String, ChannelStats> channelEntry : entry.getValue().entrySet()) {
            if (rangeDate.before(channelEntry.getValue().getStartTime())) {
               if (channelEntry.getKey().equals("config") || channelEntry.getKey().equals("heartbeat") || channelEntry.getKey().equals("monitor")) {
                  this.symmetricRowsIn = this.symmetricRowsIn + channelEntry.getValue().getDataLoaded();
                  this.symmetricRowsOut = this.symmetricRowsOut + channelEntry.getValue().getDataSent();
                  this.symmetricBytesIn = this.symmetricBytesIn + channelEntry.getValue().getDataBytesLoaded();
                  this.symmetricBytesOut = this.symmetricBytesOut + channelEntry.getValue().getDataBytesSent();
                  if (!this.systemChannelTotals.containsKey(channelEntry.getKey())) {
                     ChannelStats tempStat = new ChannelStats(
                        channelEntry.getValue().getNodeId(),
                        channelEntry.getValue().getHostName(),
                        channelEntry.getValue().getStartTime(),
                        channelEntry.getValue().getEndTime(),
                        channelEntry.getValue().getChannelId()
                     );
                     tempStat.add(channelEntry.getValue());
                     this.systemChannelTotals.put(channelEntry.getKey(), tempStat);
                  } else {
                     this.systemChannelTotals.get(channelEntry.getKey()).add(channelEntry.getValue());
                  }
               } else if (this.getReloadChannels().contains(channelEntry.getKey())) {
                  this.reloadRowsIn = this.reloadRowsIn + channelEntry.getValue().getDataLoaded();
                  this.reloadRowsOut = this.reloadRowsOut + channelEntry.getValue().getDataSent();
                  this.reloadBytesIn = this.reloadBytesIn + channelEntry.getValue().getDataBytesLoaded();
                  this.reloadBytesOut = this.reloadBytesOut + channelEntry.getValue().getDataBytesSent();
                  if (!this.reloadChannelTotals.containsKey(channelEntry.getKey())) {
                     ChannelStats tempStat = new ChannelStats(
                        channelEntry.getValue().getNodeId(),
                        channelEntry.getValue().getHostName(),
                        channelEntry.getValue().getStartTime(),
                        channelEntry.getValue().getEndTime(),
                        channelEntry.getValue().getChannelId()
                     );
                     tempStat.add(channelEntry.getValue());
                     this.reloadChannelTotals.put(channelEntry.getKey(), tempStat);
                  } else {
                     this.reloadChannelTotals.get(channelEntry.getKey()).add(channelEntry.getValue());
                  }
               } else {
                  this.cdcRowsIn = this.cdcRowsIn + channelEntry.getValue().getDataLoaded();
                  if (this.cdcRowsOut != channelEntry.getValue().getDataSent() && channelEntry.getValue().getDataSent() > 0L && this.cdcRowsOut > 0L) {
                     this.log.debug("Adding " + channelEntry.getValue().getDataSent() + " rows to cdc out " + this.cdcRowsOut);
                  }

                  this.cdcRowsOut = this.cdcRowsOut + channelEntry.getValue().getDataSent();
                  this.cdcBytesIn = this.cdcBytesIn + channelEntry.getValue().getDataBytesLoaded();
                  this.cdcBytesOut = this.cdcBytesOut + channelEntry.getValue().getDataBytesSent();
                  if (!this.cdcChannelTotals.containsKey(channelEntry.getKey())) {
                     ChannelStats tempStat = new ChannelStats(
                        channelEntry.getValue().getNodeId(),
                        channelEntry.getValue().getHostName(),
                        channelEntry.getValue().getStartTime(),
                        channelEntry.getValue().getEndTime(),
                        channelEntry.getValue().getChannelId()
                     );
                     tempStat.add(channelEntry.getValue());
                     this.cdcChannelTotals.put(channelEntry.getKey(), tempStat);
                  } else {
                     this.cdcChannelTotals.get(channelEntry.getKey()).add(channelEntry.getValue());
                  }
               }

               if (channelEntry.getValue().getStartTime() != null && channelEntry.getValue().getStartTime().before(startTime)) {
                  startTime = channelEntry.getValue().getStartTime();
               }
            }
         }

         if (this.log.isDebugEnabled() && (this.cdcRowsIn > 0L || this.cdcRowsOut > 0L)) {
            this.log.debug("Throughput: Date-" + entry.getKey() + ", Rows Out: " + this.cdcRowsOut + ", Rows In: " + this.cdcRowsIn);
         }
      }

      return this.cdcRowsOut + this.cdcRowsIn;
   }

   public void incrementTableRows(Map<String, Map<String, Long>> tableCounts, boolean loaded) {
      if (this.tableStats == null) {
         this.tableStats = new TableStats(new Date());
      }

      if (tableCounts != null) {
         for (Entry<String, Map<String, Long>> entry : tableCounts.entrySet()) {
            if (!entry.getKey().toLowerCase().startsWith(this.engine.getSymmetricDialect().getTablePrefix())) {
               Map<String, TableStat> tableEntry = this.tableStats.getTableCounts().get(entry.getKey());
               if (tableEntry == null) {
                  Map<String, TableStat> dmlMap = new ConcurrentHashMap<>();

                  for (Entry<String, Long> dmlEntry : entry.getValue().entrySet()) {
                     dmlMap.put(dmlEntry.getKey(), new TableStat(loaded ? 0L : dmlEntry.getValue(), loaded ? dmlEntry.getValue() : 0L));
                  }

                  this.tableStats.getTableCounts().put(entry.getKey(), dmlMap);
               } else {
                  for (Entry<String, Long> dmlEntry : entry.getValue().entrySet()) {
                     TableStat stat = tableEntry.get(dmlEntry.getKey());
                     if (stat == null) {
                        this.tableStats
                           .getTableCounts()
                           .get(entry.getKey())
                           .put(dmlEntry.getKey(), new TableStat(loaded ? 0L : dmlEntry.getValue(), loaded ? dmlEntry.getValue() : 0L));
                     } else {
                        if (loaded) {
                           stat.incrementLoadedCount(dmlEntry.getValue());
                        } else {
                           stat.incrementExtractedCount(dmlEntry.getValue());
                        }

                        this.tableStats.getTableCounts().get(entry.getKey()).put(dmlEntry.getKey(), stat);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public long getSymmetricRowsIn() {
      return this.symmetricRowsIn;
   }

   @Override
   public long getSymmetricRowsOut() {
      return this.symmetricRowsOut;
   }

   @Override
   public long getSymmetricBytesIn() {
      return this.symmetricBytesIn;
   }

   @Override
   public long getSymmetricBytesOut() {
      return this.symmetricBytesOut;
   }

   @Override
   public long getReloadRowsIn() {
      return this.reloadRowsIn;
   }

   @Override
   public long getReloadRowsOut() {
      return this.reloadRowsOut;
   }

   @Override
   public long getReloadBytesIn() {
      return this.reloadBytesIn;
   }

   @Override
   public long getReloadBytesOut() {
      return this.reloadBytesOut;
   }

   @Override
   public long getCdcRowsIn() {
      return this.cdcRowsIn;
   }

   @Override
   public long getCdcRowsOut() {
      return this.cdcRowsOut;
   }

   @Override
   public long getCdcBytesIn() {
      return this.cdcBytesIn;
   }

   @Override
   public long getCdcBytesOut() {
      return this.cdcBytesOut;
   }

   @Override
   public Map<String, ChannelStats> getCdcChannelTotals() {
      return this.cdcChannelTotals;
   }

   @Override
   public Map<String, ChannelStats> getReloadChannelTotals() {
      return this.reloadChannelTotals;
   }

   @Override
   public Map<String, ChannelStats> getSystemChannelTotals() {
      return this.systemChannelTotals;
   }

   protected Set<String> getReloadChannels() {
      Set<String> reloadChannels = this.reloadChannelsCache;
      if (System.currentTimeMillis() - this.reloadChannelCacheTime >= this.reloadChannelCacheTimeoutInMs || this.reloadChannelsCache.size() == 0) {
         synchronized (this) {
            this.reloadChannelsCache.clear();

            for (Entry<String, Channel> entry : this.configurationService.getChannels(false).entrySet()) {
               if (entry.getValue().isReloadFlag()) {
                  this.reloadChannelsCache.add(entry.getKey());
               }
            }

            this.reloadChannelCacheTime = System.currentTimeMillis();
            reloadChannels = this.reloadChannelsCache;
         }
      }

      return reloadChannels;
   }

   private IProConsoleService getProConsoleService() {
      if (this.proConsoleService == null) {
         this.proConsoleService = (IProConsoleService)this.engine.getExtensionService().getExtensionPoint(IProConsoleService.class);
      }

      return this.proConsoleService;
   }

   public String getMostRecentActiveTableSynced() {
      return this.getProConsoleService().getMostRecentActiveTableSynced();
   }

   public Map<Integer, Date> getTotalLoadedRows() {
      return this.getProConsoleService().getTotalLoadedRows();
   }
}
