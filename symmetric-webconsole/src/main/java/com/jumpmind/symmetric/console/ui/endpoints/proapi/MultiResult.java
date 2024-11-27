package com.jumpmind.symmetric.console.ui.endpoints.proapi;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.NodeMonitors;

import java.util.ArrayList;

public class MultiResult {
    public String message; // or create a getter if you don't like public
    public ArrayList<Monitor> monitors;
    public ArrayList<NodeMonitors> nodeMonitors;

}
