package com.jumpmind.symmetric.console.ui.data;

public class HillaOutgoingBatch {
    public long batchId = -1L;
    public String nodeId;
    public String channelId;
    public boolean bulkLoaderFlag;
    public boolean errorFlag;
    public long failedLineNumber;
    public String summary;
    public long processedRowCount;
    public String status;
    public int percent;
}
