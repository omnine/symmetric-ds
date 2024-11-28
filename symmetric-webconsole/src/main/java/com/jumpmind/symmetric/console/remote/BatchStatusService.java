package com.jumpmind.symmetric.console.remote;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jumpmind.symmetric.model.OutgoingBatch;
import org.jumpmind.symmetric.model.AbstractBatch.Status;

public class BatchStatusService implements IBatchStatusService {
   private static final int MAX_CACHE_SIZE = 1000;
   private Map<String, BatchStatus> batchStatusCache = new LinkedHashMap<String, BatchStatus>() {
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean removeEldestEntry(Entry<String, BatchStatus> eldest) {
         return this.size() > 1000;
      }
   };
   private Map<String, BatchStatus> loadBatchStatusCache = new LinkedHashMap<String, BatchStatus>() {
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean removeEldestEntry(Entry<String, BatchStatus> eldest) {
         return this.size() > 1000;
      }
   };

   @Override
   public void addToCache(BatchStatus status) {
      this.batchStatusCache.put(status.getNodeBatchId(), status);
   }

   @Override
   public void addToLoadCache(Long requestId, BatchStatus status) {
      this.loadBatchStatusCache.put(requestId + "-" + status.getNodeBatchId(), status);
   }

   @Override
   public void removeFromLoadCache(Long requestId, BatchStatus status) {
      this.loadBatchStatusCache.remove(requestId + "-" + status.getNodeBatchId());
   }

   @Override
   public void removeFromCache(BatchStatus status) {
      this.batchStatusCache.remove(status.getNodeBatchId());
   }

   @Override
   public Map<String, BatchStatus> getCachedBatches() {
      return this.batchStatusCache;
   }

   @Override
   public Map<String, BatchStatus> getLoadCachedBatches() {
      return this.loadBatchStatusCache;
   }

   @Override
   public BatchStatus getLatestStatus(OutgoingBatch batch) {
      BatchStatus cachedStatus = this.batchStatusCache.get(batch.getNodeBatchId());
      return (cachedStatus == null || !cachedStatus.getStatus().equals(Status.LD.name()) || batch.getStatus() == Status.OK)
            && (cachedStatus == null || !cachedStatus.getStatus().equals(Status.OK.name()) || batch.getStatus() == Status.OK)
         ? null
         : cachedStatus;
   }

   @Override
   public BatchStatus getLatestLoadStatus(Long requestId, OutgoingBatch batch) {
      BatchStatus cachedStatus = this.loadBatchStatusCache.get(requestId + "-" + batch.getNodeBatchId());
      return (cachedStatus == null || !cachedStatus.getStatus().equals(Status.LD.name()) || batch.getStatus() == Status.OK)
            && (cachedStatus == null || !cachedStatus.getStatus().equals(Status.OK.name()) || batch.getStatus() == Status.OK)
         ? null
         : cachedStatus;
   }
}
