package com.jumpmind.symmetric.console.ui.data;

public class HealthInfo {
    private int totalOfflineNodes = 0;
    public int totalIncomingErrors = 0;
    public int totalOutgoingErrors = 0;


    public int getOfflineNodes() {
        return totalOfflineNodes;
    }
    public void setOfflineNodes(int totalOfflineNodes) {
        this.totalOfflineNodes = totalOfflineNodes;
    }

}
