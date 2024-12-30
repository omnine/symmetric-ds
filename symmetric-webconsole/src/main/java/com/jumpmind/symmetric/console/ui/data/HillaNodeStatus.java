package com.jumpmind.symmetric.console.ui.data;

public class HillaNodeStatus {
    public String nodeId;
    public int minMaxBatchToSend;
    public int minMaxDataToRoute;
    public String lastIncomingTime;
    public String lastOutgoingTime;
    public long incomingDataCountRemaining;
    public long outgoingDataCountRemaining;
    public int incomingBatchCountRemaining;
    public int outgoingBatchCountRemaining;
    public boolean outgoingErrorFlag;
    public boolean incomingErrorFlag;
    public boolean outgoingProcessingErrorFlag;
    public boolean incomingProcessingErrorFlag;
    public boolean batchesInErrorWithAnyNode;
    public String status;
    public double averageRowsPerMilli; 
}
