package com.jumpmind.symmetric.console.service;

import com.jumpmind.symmetric.console.model.MonitorSummary;
import com.jumpmind.symmetric.console.model.NodeGroupSummary;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jumpmind.extension.IExtensionPoint;
import org.jumpmind.extension.IProgressListener;
import org.jumpmind.properties.TypedProperties;
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.symmetric.model.OutgoingBatch;
import org.jumpmind.symmetric.model.TableReloadStatus;

public interface IProConsoleService extends IExtensionPoint {
   void cancelLoad(TableReloadStatus var1);

   TypedProperties getSettings(String var1);

   void saveSettings(TypedProperties var1, String var2);

   void balanceChannelsForInitialLoad(String var1, String var2, int var3, String var4, boolean var5, String var6, IProgressListener var7);

   void ignoreRowForOutgoingBatchByDataId(OutgoingBatch var1, long var2);

   void ignoreRowForOutgoingBatchByRowNumber(OutgoingBatch var1);

   void saveTableStats(String var1, String var2, long var3, long var5, Date var7, Date var8);

   List<MonitorSummary> getMonitorSummary();

   List<NodeGroupSummary> getNodeGroupSummary();

   List<Node> getNodeSyncSummary(boolean var1);

   String getMostRecentActiveTableSynced();

   Map<Integer, Date> getTotalLoadedRows();

   List<Node> geSlowestPurgeNodes(int var1);

   List<Node> getOldestPurgeNodes(int var1);

   long geAveragePurge();

   List<Node> geSlowestRoutingNodes(int var1);

   List<Node> getOldestRoutingNodes(int var1);

   long geAverageRouting();
}
