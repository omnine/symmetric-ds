package com.jumpmind.symmetric.console.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jumpmind.symmetric.model.IModelObject;

public class Monitor implements IModelObject {
   private static final long serialVersionUID = 1L;
   public static final int INFO = 100;
   public static final int WARNING = 200;
   public static final int SEVERE = 300;
   public static final String INFO_NAME = "INFO";
   public static final String WARNING_NAME = "WARNING";
   public static final String SEVERE_NAME = "SEVERE";
   public static Map<Integer, String> severityLevelNames = new HashMap<>();
   protected String monitorId;
   protected String nodeGroupId;
   protected String externalId;
   protected String type;
   protected String expression;
   protected long threshold;
   protected int runPeriod;
   protected int runCount;
   protected int severityLevel;
   protected int displayOrder;
   protected boolean isInsight;
   protected boolean isPinned;
   protected boolean enabled;
   protected Date createTime;
   protected String lastUpdateBy;
   protected Date lastUpdateTime;
   protected transient String targetNode;

   public String getMonitorId() {
      return this.monitorId;
   }

   public void setMonitorId(String monitorId) {
      this.monitorId = monitorId;
   }

   public String getExternalId() {
      return this.externalId;
   }

   public void setExternalId(String externalId) {
      this.externalId = externalId;
   }

   public String getNodeGroupId() {
      return this.nodeGroupId;
   }

   public void setNodeGroupId(String nodeGroupId) {
      this.nodeGroupId = nodeGroupId;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public long getThreshold() {
      return this.threshold;
   }

   public void setThreshold(long threshhold) {
      this.threshold = threshhold;
   }

   public int getRunPeriod() {
      return this.runPeriod;
   }

   public void setRunPeriod(int runPeriod) {
      this.runPeriod = runPeriod;
   }

   public int getRunCount() {
      return this.runCount;
   }

   public void setRunCount(int runCount) {
      this.runCount = runCount;
   }

   public int getSeverityLevel() {
      return this.severityLevel;
   }

   public void setSeverityLevel(int severityLevel) {
      this.severityLevel = severityLevel;
   }

   public int getDisplayOrder() {
      return this.displayOrder;
   }

   public void setDisplayOrder(int displayOrder) {
      this.displayOrder = displayOrder;
   }

   public boolean isInsight() {
      return this.isInsight;
   }

   public void setInsight(boolean isInsight) {
      this.isInsight = isInsight;
   }

   public boolean isPinned() {
      return this.isPinned;
   }

   public void setPinned(boolean isPinned) {
      this.isPinned = isPinned;
   }

   public Date getCreateTime() {
      return this.createTime;
   }

   public void setCreateTime(Date createTime) {
      this.createTime = createTime;
   }

   public String getLastUpdateBy() {
      return this.lastUpdateBy;
   }

   public void setLastUpdateBy(String lastUpdateBy) {
      this.lastUpdateBy = lastUpdateBy;
   }

   public Date getLastUpdateTime() {
      return this.lastUpdateTime;
   }

   public void setLastUpdateTime(Date lastUpdateTime) {
      this.lastUpdateTime = lastUpdateTime;
   }

   public String getExpression() {
      return this.expression;
   }

   public void setExpression(String expression) {
      this.expression = expression;
   }

   public static Map<Integer, String> getSeverityLevelNames() {
      return severityLevelNames;
   }

   public String getSeverityLevelName() {
      String name = severityLevelNames.get(this.severityLevel);
      if (name == null) {
         name = "INFO";
      }

      return name;
   }

   public String getTargetNode() {
      if (this.targetNode == null) {
         if (this.externalId != null && !this.externalId.equals("ALL")) {
            this.targetNode = this.externalId + " only";
         } else {
            this.targetNode = this.nodeGroupId;
         }
      }

      return this.targetNode;
   }

   public void setTargetNode(String targetNode) {
      this.targetNode = targetNode;
   }

   public static Monitor createSystemMonitor(
      String monitorId,
      String externalId,
      String type,
      long threshold,
      int runPeriod,
      int runCount,
      int severityLevel,
      int displayOrder,
      boolean isInsight,
      boolean isPinned
   ) {
      Monitor monitor = new Monitor();
      monitor.setMonitorId(monitorId);
      monitor.setNodeGroupId("ALL");
      monitor.setExternalId(externalId);
      monitor.setType(type);
      monitor.setExpression("");
      monitor.setThreshold(threshold);
      monitor.setRunPeriod(runPeriod);
      monitor.setRunCount(runCount);
      monitor.setSeverityLevel(severityLevel);
      monitor.setEnabled(true);
      monitor.setDisplayOrder(displayOrder);
      monitor.setInsight(isInsight);
      monitor.setPinned(isPinned);
      monitor.setLastUpdateTime(new Date());
      monitor.setLastUpdateBy("system");
      monitor.setCreateTime(new Date());
      return monitor;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      return 31 * result + (this.monitorId == null ? 0 : this.monitorId.hashCode());
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
         Monitor other = (Monitor)obj;
         if (this.monitorId == null) {
            if (other.monitorId != null) {
               return false;
            }
         } else if (!this.monitorId.equals(other.monitorId)) {
            return false;
         }

         return true;
      }
   }

   static {
      severityLevelNames.put(100, "INFO");
      severityLevelNames.put(200, "WARNING");
      severityLevelNames.put(300, "SEVERE");
   }
}
