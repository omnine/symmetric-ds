package com.jumpmind.symmetric.statistic;

import java.util.Date;
import java.util.Map;
import org.jumpmind.symmetric.statistic.ChannelStats;
import org.jumpmind.symmetric.statistic.IStatisticManager;

public interface IThroughputStatisticManager extends IStatisticManager {
   void deleteStats(Date var1, Date var2, String var3);

   long getThroughput(Long var1);

   long getSymmetricRowsIn();

   long getSymmetricRowsOut();

   long getSymmetricBytesIn();

   long getSymmetricBytesOut();

   long getReloadRowsIn();

   long getReloadRowsOut();

   long getReloadBytesIn();

   long getReloadBytesOut();

   long getCdcRowsIn();

   long getCdcRowsOut();

   long getCdcBytesIn();

   long getCdcBytesOut();

   Map<String, ChannelStats> getCdcChannelTotals();

   Map<String, ChannelStats> getReloadChannelTotals();

   Map<String, ChannelStats> getSystemChannelTotals();
}
