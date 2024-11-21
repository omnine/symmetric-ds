package com.jumpmind.symmetric.notification;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.util.AppUtils;
import org.jumpmind.util.FormatUtils;

public final class NotificationTemplate {
   public static Map<String, String> a(ISymmetricEngine engine, MonitorEvent event) {
      Node eventNode = engine.getNodeService().findNode(event.getNodeId());
      Map<String, String> replacements = new HashMap<>();
      replacements.put("engineName", engine.getEngineName());
      replacements.put("eventCount", String.valueOf(event.getCount()));
      replacements.put("eventDetails", event.getDetails());
      replacements.put("eventHostName", event.getHostName());
      replacements.put("eventIsNotified", String.valueOf(event.isNotified()));
      replacements.put("eventIsResolved", String.valueOf(event.isResolved()));
      replacements.put("eventLastUpdateTime", FormatUtils.formatDateTimeISO(event.getLastUpdateTime()));
      replacements.put("eventMonitorId", event.getMonitorId());
      if (eventNode != null) {
         replacements.put("eventNodeExternalId", eventNode.getExternalId());
         replacements.put("eventNodeGroupId", eventNode.getNodeGroupId());
      } else {
         replacements.put("eventNodeExternalId", "<unknown>");
         replacements.put("eventNodeGroupId", "<unknown>");
      }

      replacements.put("eventNodeId", event.getNodeId());
      replacements.put("eventSeverityLevel", Monitor.getSeverityLevelNames().get(event.getSeverityLevel()));
      replacements.put("eventThreshold", String.valueOf(event.getThreshold()));
      replacements.put("eventTime", FormatUtils.formatDateTimeISO(event.getEventTime()));
      replacements.put("eventType", event.getType());
      replacements.put("eventValue", String.valueOf(event.getValue()));
      replacements.put("serverName", AppUtils.getHostName());
      return replacements;
   }

   public static Map<String, String> a(ISymmetricEngine engine, List<MonitorEvent> events) {
      Map<String, String> replacements = new HashMap<>();
      Set<String> nodeIds = new HashSet<>();
      Set<String> types = new HashSet<>();

      for (MonitorEvent event : events) {
         nodeIds.add(event.getNodeId());
         types.add(event.getType());
      }

      replacements.put("engineName", engine.getEngineName());
      replacements.put("eventCount", String.valueOf(events.size()));
      replacements.put("eventNodeCount", String.valueOf(nodeIds.size()));
      replacements.put("eventNodeIds", String.join(", ", nodeIds));
      replacements.put("eventTypes", String.join(", ", types));
      replacements.put("serverName", AppUtils.getHostName());
      return replacements;
   }
}
