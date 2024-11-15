package com.jumpmind.symmetric.console.ui.data;

public class HealthInfo {
    private int totalOfflineNodes = 0;
    private int totalOutgoingErrors = 0;


    public int getOfflineNodes() {
        return totalOfflineNodes;
    }
    public void setOfflineNodes(int totalOfflineNodes) {
        this.totalOfflineNodes = totalOfflineNodes;
    }

    public int getOutgoingErrors() {
        return totalOutgoingErrors;
    }
    
    public void setOutgoingErrors(int totalOutgoingErrors) {
        this.totalOutgoingErrors = totalOutgoingErrors;
    }
}
