package com.jumpmind.symmetric.console.service.impl;

import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.symmetric.service.impl.AbstractSqlMap;

public class ProConsoleServiceSqlMap extends AbstractSqlMap {
   public ProConsoleServiceSqlMap(IDatabasePlatform platform, String tablePrefix) {
      super(platform, tablePrefix);
      this.putSql("ignoreRowSql", "delete from $(data_event) where data_id = ? and batch_id = ?");
      this.putSql(
         "insertTableStat",
         "insert into $(console_table_stats) (table_name, event_type, start_time, end_time, loaded_rows, extracted_rows) values (?,?,?,?,?,?)"
      );
      this.putSql(
         "updateTableStat",
         "update $(console_table_stats) set loaded_rows=loaded_rows+?, extracted_rows=extracted_rows+? where table_name=? and event_type=? and start_time=? and end_time=?"
      );
      this.putSql(
         "updateTableStatNoParamsInSelect",
         "update $(console_table_stats) set loaded_rows=loaded_rows+$(loadedRows), extracted_rows=extracted_rows+$(extractedRows) where table_name=? and event_type=? and start_time=? and end_time=?"
      );
      this.putSql("purgeTableStatSql", "delete from $(console_table_stats) where end_time < ?");
      this.putSql(
         "getMonitorSummary",
         "select count(me.monitor_id) as node_count, m.monitor_id, m.severity_level,  max(event_count) as max_event_count, max(event_time) as max_event_time, max(m.threshold) as threshold, m.type, sum(me.event_value) as event_value from sym_monitor m left join sym_monitor_event me on me.monitor_id = m.monitor_id and is_resolved = 0  group by m.monitor_id, m.severity_level, m.threshold, m.type"
      );
      this.putSql(
         "getNodeGroupSummary",
         "select count(ttr.trigger_id) target_table_count, source_table_count, nodes, node_group_id, database_type from (      select count(str.trigger_id) source_table_count, nd.nodes, nd.node_group_id, nd.database_type from (            select count(node_id) nodes, n.node_group_id, database_type             from sym_node n             group by node_group_id, database_type            ) nd       left join sym_router sr on sr.source_node_group_id = nd.node_group_id      left join sym_trigger_router str on str.router_id = sr.router_id       group by nodes, node_group_id, database_type      ) st left join sym_router tr on tr.target_node_group_id = st.node_group_id left join sym_trigger_router ttr on ttr.router_id = tr.router_id  group by source_table_count, nodes, node_group_id, database_type "
      );
      this.putSql(
         "getNodeSyncSummary",
         "select node_group_id, external_id, database_type, database_version, database_name,  schema_version, symmetric_version, sync_url, data_rows_loaded_count, data_rows_to_send_count,  sync_enabled, batch_to_send_count, batch_in_error_count, batch_last_successful, most_recent_active_table,  purge_outgoing_average_ms, purge_outgoing_last_run_ms, purge_outgoing_last_finish, routing_average_run_ms, routing_last_run_ms, routing_last_finish, sym_data_size,  created_at_node_id, deployment_type, deployment_sub_type, config_version, data_rows_to_send_count,  data_rows_loaded_count,oldest_load_time, node_id  from $(node)  where created_at_node_id is not null order by batch_to_send_count desc"
      );
      this.putSql(
         "getNodeSyncSummaryChildOnly",
         "select n.node_group_id, n.external_id, n.database_type, n.database_version, n.database_name,   n.schema_version, n.symmetric_version, n.sync_url, n.data_rows_loaded_count, n.data_rows_to_send_count,   n.sync_enabled, n.batch_to_send_count, n.batch_in_error_count, n.batch_last_successful, n.most_recent_active_table,   n.purge_outgoing_average_ms, n.purge_outgoing_last_run_ms, n.purge_outgoing_last_finish, n.routing_average_run_ms, n.routing_last_run_ms, n.routing_last_finish, n.sym_data_size,   n.created_at_node_id, n.deployment_type, n.deployment_sub_type, n.config_version, n.data_rows_to_send_count,   n.data_rows_loaded_count, n.oldest_load_time, n.node_id from $(node) rn join $(node) n on n.created_at_node_id = rn.node_id where rn.created_at_node_id is null order by n.batch_in_error_count desc, n.batch_to_send_count desc"
      );
      this.putSql("getLastTableSynced", "select table_name, (loaded_rows + extracted_rows) as total_rows  from $(console_table_stats) order by start_time desc");
      this.putSql(
         "getTotalRowsLoaded", "select min(start_time) as oldest_load_time, sum(loaded_rows + extracted_rows) as total_rows  from $(console_table_stats)"
      );
      this.putSql("getOldestPurgeNodes", "select * from $(node) where purge_outgoing_last_finish is not null order by purge_outgoing_last_finish ");
      this.putSql("getSlowestPurgeNodes", "select * from $(node) where purge_outgoing_last_run_ms is not null order by purge_outgoing_last_run_ms desc");
      this.putSql("getAveragePurge", "select avg(purge_outgoing_last_run_ms) from $(node)");
      this.putSql("getOldestRoutingNodes", "select * from $(node) where purge_outgoing_last_finish is not null order by purge_outgoing_last_finish ");
      this.putSql("getSlowestRoutingNodes", "select * from $(node) where routing_average_run_ms is not null order by routing_average_run_ms desc");
      this.putSql("getAverageRouting", "select avg(routing_average_run_ms) from $(node)");
   }
}
