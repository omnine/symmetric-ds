package com.jumpmind.symmetric.console.model;

import java.util.Date;

public class MonitorEvent {
   protected String monitorId;
   protected String nodeId;
   protected Date eventTime;
   protected String hostName;
   protected String type;
   protected long threshold;
   protected long value;
   protected int count = 0;
   protected int severityLevel;
   protected boolean isResolved;
   protected boolean isNotified;
   protected boolean isInsight;
   protected Date notBefore;
   protected int approvedOption;
   protected String approvedBy;
   protected boolean isApprovalProcessed;
   protected Date lastUpdateTime;
   protected String details;

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.eventTime == null ? 0 : this.eventTime.hashCode());
      result = 31 * result + (this.monitorId == null ? 0 : this.monitorId.hashCode());
      return 31 * result + (this.nodeId == null ? 0 : this.nodeId.hashCode());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         MonitorEvent other = (MonitorEvent)obj;
         if (this.eventTime == null) {
            if (other.eventTime != null) {
               return false;
            }
         } else if (!this.eventTime.equals(other.eventTime)) {
            return false;
         }

         if (this.monitorId == null) {
            if (other.monitorId != null) {
               return false;
            }
         } else if (!this.monitorId.equals(other.monitorId)) {
            return false;
         }

         if (this.nodeId == null) {
            if (other.nodeId != null) {
               return false;
            }
         } else if (!this.nodeId.equals(other.nodeId)) {
            return false;
         }

         return true;
      }
   }

   public String getMonitorId() {
      return this.monitorId;
   }

   public void setMonitorId(String monitorId) {
      this.monitorId = monitorId;
   }

   public String getNodeId() {
      return this.nodeId;
   }

   public void setNodeId(String nodeId) {
      this.nodeId = nodeId;
   }

   public String getHostName() {
      return this.hostName;
   }

   public void setHostName(String hostName) {
      this.hostName = hostName;
   }

   public Date getEventTime() {
      return this.eventTime;
   }

   public void setEventTime(Date eventTime) {
      this.eventTime = eventTime;
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

   public long getThreshold() {
      return this.threshold;
   }

   public void setThreshold(long threshold) {
      this.threshold = threshold;
   }

   public int getSeverityLevel() {
      return this.severityLevel;
   }

   public void setSeverityLevel(int severityLevel) {
      this.severityLevel = severityLevel;
   }

   public boolean isNotified() {
      return this.isNotified;
   }

   public void setNotified(boolean isNotified) {
      this.isNotified = isNotified;
   }

   public int getCount() {
      return this.count;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public boolean isResolved() {
      return this.isResolved;
   }

   public void setResolved(boolean isResolved) {
      this.isResolved = isResolved;
   }

   public boolean isInsight() {
      return this.isInsight;
   }

   public void setInsight(boolean isInsight) {
      this.isInsight = isInsight;
   }

   public Date getNotBefore() {
      return this.notBefore;
   }

   public void setNotBefore(Date notBefore) {
      this.notBefore = notBefore;
   }

   public int getApprovedOption() {
      return this.approvedOption;
   }

   public void setApprovedOption(int approvedOption) {
      this.approvedOption = approvedOption;
   }

   public String getApprovedBy() {
      return this.approvedBy;
   }

   public void setApprovedBy(String approvedBy) {
      this.approvedBy = approvedBy;
   }

   public boolean isApprovalProcessed() {
      return this.isApprovalProcessed;
   }

   public void setApprovalProcessed(boolean isApprovalProcessed) {
      this.isApprovalProcessed = isApprovalProcessed;
   }

   public Date getLastUpdateTime() {
      return this.lastUpdateTime;
   }

   public void setLastUpdateTime(Date lastUpdateTime) {
      this.lastUpdateTime = lastUpdateTime;
   }

   public String getDetails() {
      return this.details;
   }

   public void setDetails(String details) {
      this.details = details;
   }

   public String getPrettyPrint() {
      StringBuffer sb = new StringBuffer(String.valueOf(this.value));
      if ("batchError".equals(this.getType())) {
         sb.append(this.getValue() > 1L ? " batches" : " batch");
      } else if ("batchUnsent".equals(this.getType())) {
         sb.append(this.getValue() > 1L ? " batches" : " batch");
      } else if ("dataUnrouted".equals(this.getType())) {
         sb.append(this.getValue() > 1L ? " rows" : " row");
      } else if ("offlineNodes".equals(this.getType())) {
         sb.append(this.getValue() > 1L ? " nodes" : " node");
      } else if ("cpu".equals(this.getType())) {
         sb.append("%");
      } else if ("memory".equals(this.getType())) {
         sb.append("%");
      } else if ("disk".equals(this.getType())) {
         sb.append("%");
      }

      return sb.toString();
   }
}
