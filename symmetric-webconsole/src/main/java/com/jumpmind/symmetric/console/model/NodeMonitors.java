package com.jumpmind.symmetric.console.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NodeMonitors implements Serializable {
   private static final long serialVersionUID = 1L;
   String nodeId;
   Map<String, MonitorEvent> monitorEvents = new HashMap<>();

   public NodeMonitors(String nodeId) {
      this.nodeId = nodeId;
   }

   public Map<String, MonitorEvent> getMonitorEvents() {
      return this.monitorEvents;
   }

   public void setMonitorEvents(Map<String, MonitorEvent> monitorEvents) {
      this.monitorEvents = monitorEvents;
   }

   public String getNodeId() {
      return this.nodeId;
   }

   public void setNodeId(String nodeId) {
      this.nodeId = nodeId;
   }
}
