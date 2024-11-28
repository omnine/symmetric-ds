package com.jumpmind.symmetric.console.remote;

import java.util.Map;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.model.OutgoingBatch;

public interface IBatchStatusService extends IBuiltInExtensionPoint {
   void addToCache(BatchStatus var1);

   void addToLoadCache(Long var1, BatchStatus var2);

   void removeFromCache(BatchStatus var1);

   void removeFromLoadCache(Long var1, BatchStatus var2);

   Map<String, BatchStatus> getCachedBatches();

   Map<String, BatchStatus> getLoadCachedBatches();

   BatchStatus getLatestStatus(OutgoingBatch var1);

   BatchStatus getLatestLoadStatus(Long var1, OutgoingBatch var2);
}
