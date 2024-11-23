package com.jumpmind.symmetric.console.model;

import java.util.Date;

public class MonitorSummary {
   int nodeCount;
   String monitorId;
   int severityLevel;
   int maxEventCount;
   Date maxEventTime;
   long threshold;
   String type;
   long value;

   public int getNodeCount() {
      return this.nodeCount;
   }

   public void setNodeCount(int nodeCount) {
      this.nodeCount = nodeCount;
   }

   public String getMonitorId() {
      return this.monitorId;
   }

   public void setMonitorId(String monitorId) {
      this.monitorId = monitorId;
   }

   public int getSeverityLevel() {
      return this.severityLevel;
   }

   public void setSeverityLevel(int severityLevel) {
      this.severityLevel = severityLevel;
   }

   public int getMaxEventCount() {
      return this.maxEventCount;
   }

   public void setMaxEventCount(int maxEventCount) {
      this.maxEventCount = maxEventCount;
   }

   public Date getMaxEventTime() {
      return this.maxEventTime;
   }

   public void setMaxEventTime(Date maxEventTime) {
      this.maxEventTime = maxEventTime;
   }

   public long getThreshold() {
      return this.threshold;
   }

   public void setThreshold(long threshold) {
      this.threshold = threshold;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public long getValue() {
      return this.value;
   }

   public void setValue(long value) {
      this.value = value;
   }

   public String printThreshold() {
      StringBuffer sb = new StringBuffer("node");
      if (this.nodeCount > 1) {
         sb.append("s");
      }

      if (this.getNodeCount() > 0) {
         if ("batchError".equals(this.getType())) {
            sb.append(" with " + this.getThreshold() + "+ batch errors");
         } else if ("batchUnsent".equals(this.getType())) {
            sb.append(" with " + this.getThreshold() + "+ unsent batches");
         } else if ("dataUnrouted".equals(this.getType())) {
            sb.append(" with " + this.getThreshold() + "+ unrouted data");
         } else if ("offlineNodes".equals(this.getType())) {
            sb.append("");
         } else if ("cpu".equals(this.getType())) {
            sb.append(" over " + this.getThreshold() + "% ");
         } else if ("memory".equals(this.getType())) {
            sb.append(" over " + this.getThreshold() + "% ");
         } else if ("disk".equals(this.getType())) {
            sb.append(" over " + this.getThreshold() + "% ");
         }
      }

      return sb.toString();
   }
}
