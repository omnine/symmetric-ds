package com.jumpmind.symmetric.console.service.impl;

import java.util.Map;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.symmetric.service.impl.AbstractSqlMap;

public class MonitorServiceSqlMap extends AbstractSqlMap {
   private String type = "type";

   public MonitorServiceSqlMap(IDatabasePlatform platform, Map<String, String> replacementTokens) {
      super(platform, replacementTokens);
      if (platform.getName().equals("interbase")) {
         String delimiter = platform.getDatabaseInfo().getDelimiterToken();
         delimiter = delimiter != null ? delimiter : "";
         this.type = delimiter + "TYPE" + delimiter;
      }

      this.putSql(
         "selectMonitorSql",
         "select monitor_id, external_id, node_group_id, "
            + this.type
            + ", expression, enabled, threshold, run_period, run_count, severity_level, display_order, is_insight, is_pinned, create_time, last_update_by, last_update_time from $(monitor)"
      );
      this.putSql(
         "selectMonitorWhereNotResolved",
         "select m.monitor_id, m.external_id, m.node_group_id, m."
            + this.type
            + ", m.expression, m.enabled, m.threshold, m.run_period, m.run_count, m.severity_level, m.display_order, m.is_insight, m.is_pinned, m.create_time, m.last_update_by, m.last_update_time, me.is_resolved from $(monitor) m inner join $(monitor_event) me on m.monitor_id = me.monitor_id where (m.node_group_id = ? or m.node_group_id = 'ALL') and (m.external_id = ? or m.external_id = 'ALL') and m.enabled = 1 and me.is_resolved = 0"
      );
      this.putSql("whereMonitorByNodeSql", "where (node_group_id = ? or node_group_id = 'ALL') and (external_id = ? or external_id = 'ALL') and enabled = 1");
      this.putSql("whereMonitorIdLikeSql", "where monitor_id like ?");
      this.putSql(
         "insertMonitorSql",
         "insert into $(monitor) (monitor_id, external_id, node_group_id, "
            + this.type
            + ", expression, enabled, threshold, run_period, run_count, severity_level, display_order, is_insight, is_pinned, create_time, last_update_by, last_update_time) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      );
      this.putSql(
         "updateMonitorSql",
         "update $(monitor) set external_id = ?, node_group_id = ?, "
            + this.type
            + " = ?, expression = ?, enabled = ?, threshold = ?, run_period = ?, run_count = ?, severity_level = ?, display_order = ?, is_insight = ?, is_pinned = ?, last_update_by = ?, last_update_time = ? where monitor_id = ?"
      );
      this.putSql("deleteMonitorSql", "delete from $(monitor) where monitor_id = ?");
      this.putSql(
         "selectMonitorEventSql",
         "select monitor_id, node_id, event_time, "
            + this.type
            + ", event_value, event_count, threshold, severity_level, host_name, is_resolved, is_notified, is_insight, not_before, approved_option, approved_by, is_approval_processed, details, last_update_time from $(monitor_event) "
      );
      this.putSql("whereMonitorEventResolvedSql", "where node_id = ? and is_resolved = 1");
      this.putSql("whereMonitorEventNotResolvedSql", "where node_id = ? and is_resolved = 0");
      this.putSql("whereMonitorEventFilteredSql", "where severity_level >= ?");
      this.putSql("whereMonitorEventForNotificationBySeveritySql", "where is_notified = 0 and is_insight = 0 and severity_level >= ?");
      this.putSql("whereMonitorEventIdSql", "where monitor_id = ? and is_resolved = 0");
      this.putSql(
         "insertMonitorEventSql",
         "insert into $(monitor_event) (monitor_id, node_id, event_time, host_name, "
            + this.type
            + ", event_value, event_count, threshold, severity_level, is_resolved, is_notified, is_insight, not_before, approved_option, approved_by, is_approval_processed, details, last_update_time) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      );
      this.putSql(
         "updateMonitorEventSql",
         "update $(monitor_event) set host_name = ?, "
            + this.type
            + " = ?, event_value = ?, event_count = ?, threshold = ?, severity_level = ?, is_resolved = 0, is_notified = ?, is_insight = ?, not_before = ?, approved_option = ?, approved_by = ?, is_approval_processed = ?, last_update_time = ?, details = ? where monitor_id = ? and node_id = ? and event_time = ?"
      );
      this.putSql(
         "updateMonitorEventResolvedSql",
         "update $(monitor_event) set is_resolved = 1, is_notified = 0, last_update_time = ? where monitor_id = ? and node_id = ? and event_time = ?"
      );
      this.putSql(
         "updateMonitorEventUnresolvedSql",
         "update $(monitor_event) set is_resolved = 0, last_update_time = ? where monitor_id = ? and node_id = ? and event_time = ?"
      );
      this.putSql("updateMonitorEventNotifiedSql", "update $(monitor_event) set is_notified = 1 where monitor_id = ? and node_id = ? and event_time = ?");
      this.putSql("deleteMonitorEventSql", "delete from $(monitor_event) where monitor_id = ? and node_id = ? and event_time = ?");
      this.putSql(
         "selectNotificationSql",
         "select notification_id, node_group_id, external_id, severity_level, "
            + this.type
            + ", expression, enabled, create_time, last_update_by, last_update_time from $(notification)"
      );
      this.putSql(
         "whereNotificationByNodeSql", "where (node_group_id = ? or node_group_id = 'ALL') and (external_id = ? or external_id = 'ALL') and enabled = 1"
      );
      this.putSql("whereNotificationIdLikeSql", "where notification_id like ?");
      this.putSql(
         "insertNotificationSql",
         "insert into $(notification) (notification_id, node_group_id, external_id, severity_level, "
            + this.type
            + ", expression, enabled, create_time, last_update_by, last_update_time) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      );
      this.putSql(
         "updateNotificationSql",
         "update $(notification) set node_group_id = ?, external_id = ?, severity_level = ?, "
            + this.type
            + " = ?, expression = ?, enabled = ?, create_time = ?, last_update_by = ?, last_update_time = ? where notification_id = ?"
      );
      this.putSql("deleteNotificationSql", "delete from $(notification) where notification_id = ?");
   }

   public String getTypeColumnName() {
      return this.type;
   }
}
