package com.jumpmind.symmetric.console.ui.data;

public class HealthInfo {
    public int totalOfflineNodes = 0;
    public int totalIncomingErrors = 0;
    public int totalOutgoingErrors = 0;
    public int totalFailedMonitors = 0;

    public long unroutedDataCount = 0;

    public String engineNodeId;

}
