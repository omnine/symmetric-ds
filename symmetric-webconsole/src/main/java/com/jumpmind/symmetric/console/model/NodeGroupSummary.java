package com.jumpmind.symmetric.console.model;

public class NodeGroupSummary {
   private int targetTableCount;
   private int sourceTableCount;
   private int nodes;
   private String nodeGroupId;
   private String databaseType;

   public int getTargetTableCount() {
      return this.targetTableCount;
   }

   public void setTargetTableCount(int targetTableCount) {
      this.targetTableCount = targetTableCount;
   }

   public int getSourceTableCount() {
      return this.sourceTableCount;
   }

   public void setSourceTableCount(int sourceTableCount) {
      this.sourceTableCount = sourceTableCount;
   }

   public int getNodes() {
      return this.nodes;
   }

   public void setNodes(int nodes) {
      this.nodes = nodes;
   }

   public String getNodeGroupId() {
      return this.nodeGroupId;
   }

   public void setNodeGroupId(String nodeGroupId) {
      this.nodeGroupId = nodeGroupId;
   }

   public String getDatabaseType() {
      return this.databaseType;
   }

   public void setDatabaseType(String databaseType) {
      this.databaseType = databaseType;
   }
}
