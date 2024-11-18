package com.jumpmind.symmetric.console.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jumpmind.symmetric.model.IncomingBatchSummary;
import org.jumpmind.symmetric.model.OutgoingBatchSummary;

public class NodeStatus implements Serializable {
   private static final long serialVersionUID = 1L;
   String nodeId;
   int minMaxBatchToSend;
   int minMaxDataToRoute;
   Date lastIncomingTime;
   Date lastOutgoingTime;
   long incomingDataCountRemaining;
   long outgoingDataCountRemaining;
   int incomingBatchCountRemaining;
   int outgoingBatchCountRemaining;
   boolean outgoingErrorFlag;
   boolean incomingErrorFlag;
   boolean outgoingProcessingErrorFlag;
   boolean incomingProcessingErrorFlag;
   List<String> errorMessages = new ArrayList<>();
   List<OutgoingBatchSummary> outgoingSummaries = new ArrayList<>();
   List<IncomingBatchSummary> incomingSummaries = new ArrayList<>();
   boolean batchesInErrorWithAnyNode;

   public NodeStatus(String nodeId) {
      this.nodeId = nodeId;
   }

   public Date getLastIncomingTime() {
      return this.lastIncomingTime;
   }

   public void setLastIncomingTime(Date lastIncomingTime) {
      this.lastIncomingTime = lastIncomingTime;
   }

   public Date getLastOutgoingTime() {
      return this.lastOutgoingTime;
   }

   public void setLastOutgoingTime(Date lastOutgoingTime) {
      this.lastOutgoingTime = lastOutgoingTime;
   }

   public long getIncomingDataCountRemaining() {
      return this.incomingDataCountRemaining;
   }

   public void setIncomingDataCountRemaining(long incomingDataCountRemaining) {
      this.incomingDataCountRemaining = incomingDataCountRemaining;
   }

   public long getOutgoingDataCountRemaining() {
      return this.outgoingDataCountRemaining;
   }

   public void setOutgoingDataCountRemaining(int outgoingDataCountRemaining) {
      this.outgoingDataCountRemaining = (long)outgoingDataCountRemaining;
   }

   public int getIncomingBatchCountRemaining() {
      return this.incomingBatchCountRemaining;
   }

   public void setIncomingBatchCountRemaining(int incomingBatchCountRemaining) {
      this.incomingBatchCountRemaining = incomingBatchCountRemaining;
   }

   public int getOutgoingBatchCountRemaining() {
      return this.outgoingBatchCountRemaining;
   }

   public void incrementOutgoingBatchCountRemaining(int batchCount) {
      this.outgoingBatchCountRemaining += batchCount;
   }

   public void setOutgoingBatchCountRemaining(int outgoingBatchCountRemaining) {
      this.outgoingBatchCountRemaining = outgoingBatchCountRemaining;
   }

   public boolean isOutgoingErrorFlag() {
      return this.outgoingErrorFlag;
   }

   public void setOutgoingErrorFlag(boolean outgoingErrorFlag) {
      this.outgoingErrorFlag = outgoingErrorFlag;
   }

   public boolean isIncomingErrorFlag() {
      return this.incomingErrorFlag;
   }

   public void setIncomingErrorFlag(boolean incomingErrorFlag) {
      this.incomingErrorFlag = incomingErrorFlag;
   }

   public boolean isOutgoingProcessingErrorFlag() {
      return this.outgoingProcessingErrorFlag;
   }

   public void setOutgoingProcessingErrorFlag(boolean outgoingProcessingErrorFlag) {
      this.outgoingProcessingErrorFlag = outgoingProcessingErrorFlag;
   }

   public boolean isIncomingProcessingErrorFlag() {
      return this.incomingProcessingErrorFlag;
   }

   public void setIncomingProcessingErrorFlag(boolean incomingProcessingErrorFlag) {
      this.incomingProcessingErrorFlag = incomingProcessingErrorFlag;
   }

   public String getNodeId() {
      return this.nodeId;
   }

   public void setNodeId(String nodeId) {
      this.nodeId = nodeId;
   }

   public void incrementOutgoingDataCountRemaining(int dataCount) {
      this.outgoingDataCountRemaining += (long)dataCount;
   }

   public void incrementIncomingDataCountRemaining(int dataCount) {
      this.incomingDataCountRemaining += (long)dataCount;
   }

   public void incrementIncomingBatchCountRemaining(int batchCount) {
      this.incomingBatchCountRemaining += batchCount;
   }

   public List<OutgoingBatchSummary> getOutgoingSummaries() {
      return this.outgoingSummaries;
   }

   public void setOutgoingSummaries(List<OutgoingBatchSummary> outgoingSummaries) {
      this.outgoingSummaries = outgoingSummaries;
   }

   public void addOutgoingSummary(OutgoingBatchSummary summary) {
      if (this.outgoingSummaries == null) {
         this.outgoingSummaries = new ArrayList<>();
      }

      this.outgoingSummaries.add(summary);
   }

   public List<IncomingBatchSummary> getIncomingSummaries() {
      return this.incomingSummaries;
   }

   public void setIncomingSummaries(List<IncomingBatchSummary> incomingSummaries) {
      this.incomingSummaries = incomingSummaries;
   }

   public void addIncomingSummary(IncomingBatchSummary summary) {
      if (this.incomingSummaries == null) {
         this.incomingSummaries = new ArrayList<>();
      }

      this.incomingSummaries.add(summary);
   }

   public List<String> getErrorMessages() {
      return this.errorMessages;
   }

   public void setErrorMessages(List<String> errorMessages) {
      this.errorMessages = errorMessages;
   }

   public void addErrorMessage(String message) {
      if (this.errorMessages == null) {
         this.errorMessages = new ArrayList<>();
      }

      this.errorMessages.add(message);
   }

   public int getMinMaxBatchToSend() {
      return this.minMaxBatchToSend;
   }

   public void setMinMaxBatchToSend(int minMaxBatchToSend) {
      this.minMaxBatchToSend = minMaxBatchToSend;
   }

   public int getMinMaxDataToRoute() {
      return this.minMaxDataToRoute;
   }

   public void setMinMaxDataToRoute(int minMaxDataToRoute) {
      this.minMaxDataToRoute = minMaxDataToRoute;
   }

   public boolean isBatchesInErrorWithAnyNode() {
      return this.batchesInErrorWithAnyNode;
   }

   public void setBatchesInErrorWithAnyNode(boolean batchesInErrorWithAnyNode) {
      this.batchesInErrorWithAnyNode = batchesInErrorWithAnyNode;
   }

   public float getAverageRowsPerMilli() {
      float totalDataCount = 0.0F;
      float totalMillis = 0.0F;

      for (OutgoingBatchSummary summary : this.getOutgoingSummaries()) {
         totalDataCount += (float)summary.getDataCount();
         totalMillis += (float)summary.getTotalMillis();
      }

      return totalMillis > 0.0F ? totalDataCount / totalMillis : 0.0F;
   }

   public String getStatus() {
      if (this.isOutgoingErrorFlag() || this.isIncomingErrorFlag() || this.isBatchesInErrorWithAnyNode()) {
         return "1";
      } else if (!this.isOutgoingProcessingErrorFlag() && !this.isIncomingProcessingErrorFlag()) {
         return this.getIncomingDataCountRemaining() <= 0L && this.getOutgoingDataCountRemaining() <= 0L ? "4" : "3";
      } else {
         return "2";
      }
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
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
         NodeStatus other = (NodeStatus)obj;
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
}
