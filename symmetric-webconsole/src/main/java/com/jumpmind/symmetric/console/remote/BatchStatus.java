package com.jumpmind.symmetric.console.remote;

import java.util.Date;

public class BatchStatus {
   private String nodeId;
   private long batchId;
   private String status;
   private long processedRowCount;
   private long processedMillis;
   private String sqlMessage;
   private Date lastUpdated;

   public BatchStatus() {
   }

   public BatchStatus(long batchId, String status, long processedRowCount, long processedMillis) {
      this(batchId, status, processedRowCount, processedMillis, null);
   }

   public BatchStatus(long batchId, String status, long processedRowCount, long processedMillis, String sqlMessage) {
      this.batchId = batchId;
      this.status = status;
      this.processedRowCount = processedRowCount;
      this.processedMillis = processedMillis;
      this.sqlMessage = sqlMessage;
      this.lastUpdated = new Date();
   }

   public BatchStatus(long batchId, String nodeId, String status, long processedRowCount, long processedMillis, String sqlMessage) {
      this.batchId = batchId;
      this.nodeId = nodeId;
      this.status = status;
      this.processedRowCount = processedRowCount;
      this.processedMillis = processedMillis;
      this.sqlMessage = sqlMessage;
      this.lastUpdated = new Date();
   }

   public String getNodeId() {
      return this.nodeId;
   }

   public void setNodeId(String nodeId) {
      this.nodeId = nodeId;
   }

   public Long getBatchId() {
      return this.batchId;
   }

   public void setBatchId(Long batchId) {
      this.batchId = batchId;
   }

   public String getNodeBatchId() {
      return this.nodeId + "-" + this.batchId;
   }

   public String getStatus() {
      return this.status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public long getProcessedRowCount() {
      return this.processedRowCount;
   }

   public void setProcessedRowCount(long processedRowCount) {
      this.processedRowCount = processedRowCount;
   }

   public String getSqlMessage() {
      return this.sqlMessage;
   }

   public void setSqlMessage(String sqlMessage) {
      this.sqlMessage = sqlMessage;
   }

   public Date getLastUpdated() {
      return this.lastUpdated;
   }

   public void setLastUpdated(Date lastUpdated) {
      this.lastUpdated = lastUpdated;
   }

   public long getProcessedMillis() {
      return this.processedMillis;
   }

   public void setProcessedMillis(long processedMillis) {
      this.processedMillis = processedMillis;
   }
}
