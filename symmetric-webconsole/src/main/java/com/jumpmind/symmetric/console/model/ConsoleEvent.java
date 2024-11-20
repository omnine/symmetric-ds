package com.jumpmind.symmetric.console.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleEvent implements Serializable {
   static final Logger log = LoggerFactory.getLogger(ConsoleEvent.class);
   private static final long serialVersionUID = 1L;
   private String userId;
   private String eventCode;
   private Date createTime;
   private String eventNode;
   private String sourceNode;
   private String targetNode;
   private String value;
   private static List<String> allEventCodes = new ArrayList<>();
   public static final String ADD_NODE = "Add Node";
   public static final String START_NODE = "Start Node";
   public static final String STOP_NODE = "Stop Node";
   public static final String UNINSTALL_NODE = "Uninstall Node";
   public static final String EXPORT_NODES = "Export Nodes";
   public static final String ALLOW_REGISTRATION = "Allow Registration";
   public static final String REJECT_REGISTRATION = "Reject Registration";
   public static final String REOPEN_REGISTRATION = "Reopen Registration";
   public static final String UNREGISTER_NODE = "Unregister Node";
   public static final String PRIORITIZE_NODE = "Prioritize Node";
   public static final String DEPRIORITIZE_NODE = "Deprioritize Node";
   public static final String SEND_INITIAL_LOAD = "Send initial load";
   public static final String RECEIVE_INITIAL_LOAD = "Receive initial load";
   public static final String SEND_LOAD = "Send load";
   public static final String SEND_SQL = "Send sql";
   public static final String SEND_BSH = "Send bsh";
   public static final String SEND_TABLE_SCHEMA = "Send table schema";
   public static final String SEND_TABLE_DATA = "Send table data";
   public static final String START_JOB = "Start Job";
   public static final String STOP_JOB = "Stop Job";
   public static final String RUN_JOB_NOW = "Run Job Now";
   public static final String REBUILD_TRIGGER = "Rebuild trigger";
   public static final String DROP_TRIGGER = "Drop trigger";
   public static final String REBUILD_ALL_TRIGGER = "Rebuild all trigger";
   public static final String CANCEL_LOAD = "Cancel load";
   public static final String VIEW_OUTGOING_BATCH = "View outgoing batch";
   public static final String IGNORE_OUTGOING_BATCH = "Ignore outgoing batch";
   public static final String IGNORE_OUTGOING_ROW = "Ignore row in outgoing batch";
   public static final String CLEAR_STAGING_FOR_BATCH = "Clear staging for batch";
   public static final String VIEW_INCOMING_BATCH = "View incoming batch";
   public static final String IGNORE_INCOMING_BATCH = "Ignore incoming batch";
   public static final String CLEAR_INCOMING_BATCH = "Clear incoming batch";
   public static final String IGNORE_INCOMING_ROW = "Ignore row in incoming batch";
   public static final String DOWNLOAD_BATCH = "Download batch";
   public static final String CLEAR_STAGING = "Clear staging";
   public static final String DOWNLOAD_STAGING_FILE = "Download staging file";
   public static final String EDIT_STAGING_FILE = "Edit staging file";
   public static final String REMOVE_STAGING_FILE = "Remove staging file";
   public static final String EXECUTE_SQL = "Execute SQL";
   public static final String INTERRUPT_PROCESS = "Interrupt Process";
   public static final String EXPORT_CONSOLE_EVENTS = "Export Console Events";
   public static final String EXPORT_JVM_THREADS = "Export JVM Threads";
   public static final String RESOLVE_MONITOR_EVENT = "Resolve Monitor Event";
   public static final String APPROVE_INSIGHT = "Approve Insight";
   public static final String DISMISS_INSIGHT = "Dismiss Insight";
   public static final String UNDO_DISMISSAL_FOR_INSIGHT = "Undo Dismissal for Insight";
   public static final String IMPORT_CERTIFICATE_AND_KEY = "Import Certificate & Key";
   public static final String BACKUP_CERTIFICATE_AND_KEY = "Backup Certificate & Key";
   public static final String EXPORT_CERTIFICATE = "Export Certificate";
   public static final String GENERATE_SELF_SIGNED_CERTIFICATE = "Generate Certificate";
   public static final String DELETE_AUTHORITY = "Delete Authority";
   public static final String EXPORT_AUTHORITY = "Export Authority";
   public static final String IMPORT_AUTHORITY = "Import Authority";
   public static final String EDIT_PARAMETER = "Edit Parameter";
   public static final String DOWNLOAD_LOG = "Download Log";
   public static final String SAVE_LDAP_CONFIGURATION = "Save LDAP Configuration";
   public static final String SAVE_SAML_CONFIGURATION = "Save SAML Configuration";
   public static final String SAVE_MAIL_SERVER_CONFIGURATION = "Save Mail Server Config";
   public static final String INSTALL_LICENSE_KEY = "Install License key";
   public static final String EXPORT_LICENSE_KEY = "Export License key";
   public static final String IMPORT_CONFIGURATION = "Import Configuration";
   public static final String EXPORT_CONFIGURATION = "Export Configuration";
   public static final String SYNCHRONIZE_TABLES = "Synchronize Tables";
   public static final String CHANNEL_CREATED = "Channel Created";
   public static final String CHANNEL_MODIFIED = "Channel Modified";
   public static final String CHANNEL_DELETED = "Channel Deleted";
   public static final String CHANNELS_REBALANCED = "Channels Rebalanced";
   public static final String CONFLICT_CREATED = "Conflict Created";
   public static final String CONFLICT_MODIFIED = "Conflict Modified";
   public static final String CONFLICT_DELETED = "Conflict Deleted";
   public static final String CONSOLE_ROLE_CREATED = "Console Role Created";
   public static final String CONSOLE_ROLE_MODIFIED = "Console Role Modified";
   public static final String CONSOLE_ROLE_DELETED = "Console Role Deleted";
   public static final String CONSOLE_USER_EXPIRED = "Console User Expired";
   public static final String CONSOLE_USER_RESET_REQUIRED = "Console User Reset";
   public static final String CONSOLE_USER_ENABLED = "Console User Enabled";
   public static final String CONSOLE_USER_DISABLED = "Console User Disabled";
   public static final String CONSOLE_USER_LOGIN = "Console User Login";
   public static final String CONSOLE_USER_FAILED_LOGIN = "Console User Failed Login";
   public static final String CONSOLE_USER_FORGOT_PASSWORD = "Console Password Forgot";
   public static final String CONSOLE_USER_EMAIL_CHANGED = "Console Email Changed";
   public static final String CONSOLE_USER_LOGOUT = "Console User Logout";
   public static final String CONSOLE_USER_CREATED = "Console User Created";
   public static final String CONSOLE_USER_MODIFIED = "Console User Modified";
   public static final String CONSOLE_USER_DELETED = "Console User Deleted";
   public static final String CONSOLE_USER_DISCONNECTED = "Console User Disconnected";
   public static final String CONSOLE_USER_GLASS_BREAK_REQUIRED = "Console User Glass Break Required";
   public static final String CONSOLE_USER_GLASS_BROKEN = "Console User Glass Broken";
   public static final String CONSOLE_USER_GLASS_BREAK_EXPIRED = "Console User Glass Break Expired";
   public static final String CONSOLE_PASSWORD_CHANGED = "Console Password Changed";
   public static final String EXTENSION_CREATED = "Extension Created";
   public static final String EXTENSION_MODIFIED = "Extension Modified";
   public static final String EXTENSION_DELETED = "Extension Deleted";
   public static final String FILE_ROUTING_CREATED = "File Routing Created";
   public static final String FILE_ROUTING_MODIFIED = "File Routing Modified";
   public static final String FILE_ROUTING_DELETED = "File Routing Deleted";
   public static final String FILE_TRIGGER_CREATED = "File Trigger Created";
   public static final String FILE_TRIGGER_MODIFIED = "File Trigger Modified";
   public static final String FILE_TRIGGER_DELETED = "File Trigger Deleted";
   public static final String GROUP_CREATED = "Group Created";
   public static final String GROUP_MODIFIED = "Group Modified";
   public static final String GROUP_DELETED = "Group Deleted";
   public static final String GROUPLET_CREATED = "Grouplet Created";
   public static final String GROUPLET_MODIFIED = "Grouplet Modified";
   public static final String GROUPLET_DELETED = "Grouplet Deleted";
   public static final String GROUP_LINK_CREATED = "Group Link Created";
   public static final String GROUP_LINK_MODIFIED = "Group Link Modified";
   public static final String GROUP_LINK_DELETED = "Group Link Deleted";
   public static final String TABLE_GROUP_CREATED = "Table Group Created";
   public static final String TABLE_GROUP_MODIFIED = "Table Group Modified";
   public static final String TABLE_GROUP_DELETED = "Table Group Deleted";
   public static final String TABLE_GROUP_HIER_CREATED = "Table Group Hier Created";
   public static final String TABLE_GROUP_HIER_MODIFIED = "Table Group Hier Modified";
   public static final String TABLE_GROUP_HIER_DELETED = "Table Group Hier Deleted";
   public static final String JOB_CREATED = "Job Created";
   public static final String JOB_MODIFIED = "Job Modified";
   public static final String JOB_DELETED = "Job Deleted";
   public static final String LOAD_FILTER_CREATED = "Load Filter Created";
   public static final String LOAD_FILTER_MODIFIED = "Load Filter Modified";
   public static final String LOAD_FILTER_DELETED = "Load Filter Deleted";
   public static final String MONITOR_CREATED = "Monitor Created";
   public static final String MONITOR_MODIFIED = "Monitor Modified";
   public static final String MONITOR_DELETED = "Monitor Deleted";
   public static final String NODE_CHANNEL_MODIFIED = "Node Channel Modified";
   public static final String NOTIFICATION_CREATED = "Notification Created";
   public static final String NOTIFICATION_MODIFIED = "Notification Modified";
   public static final String NOTIFICATION_DELETED = "Notification Deleted";
   public static final String REST_API_KEY_CREATED = "REST API Key Created";
   public static final String REST_API_KEY_DELETED = "REST API Key Deleted";
   public static final String ROUTER_CREATED = "Router Created";
   public static final String ROUTER_MODIFIED = "Router Modified";
   public static final String ROUTER_DELETED = "Router Deleted";
   public static final String TABLE_ROUTING_CREATED = "Table Routing Created";
   public static final String TABLE_ROUTING_MODIFIED = "Table Routing Modified";
   public static final String TABLE_ROUTING_DELETED = "Table Routing Deleted";
   public static final String TABLE_TRIGGER_CREATED = "Table Trigger Created";
   public static final String TABLE_TRIGGER_MODIFIED = "Table Trigger Modified";
   public static final String TABLE_TRIGGER_DELETED = "Table Trigger Deleted";
   public static final String TRANSFORM_CREATED = "Transform Created";
   public static final String TRANSFORM_MODIFIED = "Transform Modified";
   public static final String TRANSFORM_DELETED = "Transform Deleted";
   public static final String UPLOAD_PATCH = "Upload Patch";
   public static final String DOWNLOAD_PATCH = "Download Patch";
   public static final String DELETE_PATCH = "Delete Patch";
   public static final String TAKE_SNAPSHOT = "Take Snapshot";
   public static final String CLEAR_SNAPSHOTS = "Clear Snapshots";
   public static final String DOWNLOAD_SNAPSHOT = "Download Snapshot";
   public static final String REMOVE_SNAPSHOT = "Remove Snapshot";
   public static final String INSTALL_MODULE = "Install Module";
   public static final String UPGRADE_MODULE = "Upgrade Module";
   public static final String REMOVE_MODULE = "Remove Module";
   public static final String RESET_ROW_STATISTICS = "Reset Row Statistics";
   public static final String KEYSTORE_PASSWORD_CHANGED = "Keystore Password Changed";
   public static final String DATABASE_USER_CHANGED = "Database User Changed";
   public static final String DATABASE_PASSWORD_CHANGED = "Database Password Changed";
   public static final String COMPARE_REQUESTED = "Compare Requested";
   public static final String COMPARE_CANCELLED = "Compare Cancelled";
   public static final String QUERY_NODE = "Query Node";
   public static final String LOAD_CONFIG = "Load Configuration";
   public static final String SYNC_TRIGGERS = "Synchronize Triggers";
   public static final String SYNC_TRIGGERS_FOR_TABLE = "Synchronize Triggers for Table";

   public ConsoleEvent() {
   }

   public ConsoleEvent(String userId, String eventCode, String eventNode, String sourceNode, String targetNode, String value) {
      this.userId = userId;
      this.eventCode = eventCode;
      this.eventNode = eventNode;
      this.sourceNode = sourceNode;
      this.targetNode = targetNode;
      this.value = value;
   }

   @Override
   public String toString() {
      return "ConsoleEvent [eventCode="
         + this.eventCode
         + ", userId="
         + this.userId
         + ", eventNode="
         + this.eventNode
         + ", sourceNode="
         + this.sourceNode
         + ", targetNode="
         + this.targetNode
         + ", value="
         + this.value
         + "]";
   }

   public String getUserId() {
      return this.userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getEventCode() {
      return this.eventCode;
   }

   public void setEventCode(String eventCode) {
      this.eventCode = eventCode;
   }

   public Date getCreateTime() {
      return this.createTime;
   }

   public void setCreateTime(Date createTime) {
      this.createTime = createTime;
   }

   public String getEventNode() {
      return this.eventNode;
   }

   public void setEventNode(String eventNode) {
      this.eventNode = eventNode;
   }

   public String getSourceNode() {
      return this.sourceNode;
   }

   public void setSourceNode(String sourceNode) {
      this.sourceNode = sourceNode;
   }

   public String getTargetNode() {
      return this.targetNode;
   }

   public void setTargetNode(String targetNode) {
      this.targetNode = targetNode;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public static List<String> getAllEventCodes() {
      return allEventCodes;
   }

   static {
      allEventCodes.add("Add Node");
      allEventCodes.add("Start Node");
      allEventCodes.add("Stop Node");
      allEventCodes.add("Uninstall Node");
      allEventCodes.add("Export Nodes");
      allEventCodes.add("Allow Registration");
      allEventCodes.add("Reject Registration");
      allEventCodes.add("Reopen Registration");
      allEventCodes.add("Unregister Node");
      allEventCodes.add("Prioritize Node");
      allEventCodes.add("Deprioritize Node");
      allEventCodes.add("Send initial load");
      allEventCodes.add("Receive initial load");
      allEventCodes.add("Send load");
      allEventCodes.add("Send sql");
      allEventCodes.add("Send bsh");
      allEventCodes.add("Send table schema");
      allEventCodes.add("Send table data");
      allEventCodes.add("Start Job");
      allEventCodes.add("Stop Job");
      allEventCodes.add("Run Job Now");
      allEventCodes.add("Rebuild trigger");
      allEventCodes.add("Drop trigger");
      allEventCodes.add("Rebuild all trigger");
      allEventCodes.add("Cancel load");
      allEventCodes.add("View outgoing batch");
      allEventCodes.add("Ignore outgoing batch");
      allEventCodes.add("Ignore row in outgoing batch");
      allEventCodes.add("Clear staging for batch");
      allEventCodes.add("View incoming batch");
      allEventCodes.add("Ignore incoming batch");
      allEventCodes.add("Clear incoming batch");
      allEventCodes.add("Ignore row in incoming batch");
      allEventCodes.add("Download batch");
      allEventCodes.add("Clear staging");
      allEventCodes.add("Download staging file");
      allEventCodes.add("Edit staging file");
      allEventCodes.add("Remove staging file");
      allEventCodes.add("Execute SQL");
      allEventCodes.add("Interrupt Process");
      allEventCodes.add("Export Console Events");
      allEventCodes.add("Export JVM Threads");
      allEventCodes.add("Resolve Monitor Event");
      allEventCodes.add("Approve Insight");
      allEventCodes.add("Dismiss Insight");
      allEventCodes.add("Undo Dismissal for Insight");
      allEventCodes.add("Import Certificate & Key");
      allEventCodes.add("Backup Certificate & Key");
      allEventCodes.add("Export Certificate");
      allEventCodes.add("Generate Certificate");
      allEventCodes.add("Delete Authority");
      allEventCodes.add("Export Authority");
      allEventCodes.add("Import Authority");
      allEventCodes.add("Edit Parameter");
      allEventCodes.add("Download Log");
      allEventCodes.add("Save LDAP Configuration");
      allEventCodes.add("Save SAML Configuration");
      allEventCodes.add("Save Mail Server Config");
      allEventCodes.add("Install License key");
      allEventCodes.add("Export License key");
      allEventCodes.add("Import Configuration");
      allEventCodes.add("Export Configuration");
      allEventCodes.add("Synchronize Tables");
      allEventCodes.add("Channel Created");
      allEventCodes.add("Channel Modified");
      allEventCodes.add("Channel Deleted");
      allEventCodes.add("Channels Rebalanced");
      allEventCodes.add("Conflict Created");
      allEventCodes.add("Conflict Modified");
      allEventCodes.add("Conflict Deleted");
      allEventCodes.add("Console Role Created");
      allEventCodes.add("Console Role Modified");
      allEventCodes.add("Console Role Deleted");
      allEventCodes.add("Console User Expired");
      allEventCodes.add("Console User Enabled");
      allEventCodes.add("Console User Disabled");
      allEventCodes.add("Console User Login");
      allEventCodes.add("Console User Failed Login");
      allEventCodes.add("Console Password Forgot");
      allEventCodes.add("Console Email Changed");
      allEventCodes.add("Console User Logout");
      allEventCodes.add("Console User Created");
      allEventCodes.add("Console User Modified");
      allEventCodes.add("Console User Deleted");
      allEventCodes.add("Console User Disconnected");
      allEventCodes.add("Console Password Changed");
      allEventCodes.add("Extension Created");
      allEventCodes.add("Extension Modified");
      allEventCodes.add("Extension Deleted");
      allEventCodes.add("File Routing Created");
      allEventCodes.add("File Routing Modified");
      allEventCodes.add("File Routing Deleted");
      allEventCodes.add("File Trigger Created");
      allEventCodes.add("File Trigger Modified");
      allEventCodes.add("File Trigger Deleted");
      allEventCodes.add("Group Created");
      allEventCodes.add("Group Modified");
      allEventCodes.add("Group Deleted");
      allEventCodes.add("Grouplet Created");
      allEventCodes.add("Grouplet Modified");
      allEventCodes.add("Grouplet Deleted");
      allEventCodes.add("Group Link Created");
      allEventCodes.add("Group Link Modified");
      allEventCodes.add("Group Link Deleted");
      allEventCodes.add("Table Group Created");
      allEventCodes.add("Table Group Modified");
      allEventCodes.add("Table Group Deleted");
      allEventCodes.add("Table Group Hier Created");
      allEventCodes.add("Table Group Hier Modified");
      allEventCodes.add("Table Group Hier Deleted");
      allEventCodes.add("Job Created");
      allEventCodes.add("Job Modified");
      allEventCodes.add("Job Deleted");
      allEventCodes.add("Load Filter Created");
      allEventCodes.add("Load Filter Modified");
      allEventCodes.add("Load Filter Deleted");
      allEventCodes.add("Monitor Created");
      allEventCodes.add("Monitor Modified");
      allEventCodes.add("Monitor Deleted");
      allEventCodes.add("Node Channel Modified");
      allEventCodes.add("Notification Created");
      allEventCodes.add("Notification Modified");
      allEventCodes.add("Notification Deleted");
      allEventCodes.add("REST API Key Created");
      allEventCodes.add("REST API Key Deleted");
      allEventCodes.add("Router Created");
      allEventCodes.add("Router Modified");
      allEventCodes.add("Router Deleted");
      allEventCodes.add("Table Routing Created");
      allEventCodes.add("Table Routing Modified");
      allEventCodes.add("Table Routing Deleted");
      allEventCodes.add("Table Trigger Created");
      allEventCodes.add("Table Trigger Modified");
      allEventCodes.add("Table Trigger Deleted");
      allEventCodes.add("Transform Created");
      allEventCodes.add("Transform Modified");
      allEventCodes.add("Transform Deleted");
      allEventCodes.add("Upload Patch");
      allEventCodes.add("Download Patch");
      allEventCodes.add("Delete Patch");
      allEventCodes.add("Take Snapshot");
      allEventCodes.add("Clear Snapshots");
      allEventCodes.add("Download Snapshot");
      allEventCodes.add("Remove Snapshot");
      allEventCodes.add("Install Module");
      allEventCodes.add("Upgrade Module");
      allEventCodes.add("Remove Module");
      allEventCodes.add("Reset Row Statistics");
      allEventCodes.add("Keystore Password Changed");
      allEventCodes.add("Database User Changed");
      allEventCodes.add("Database Password Changed");
      allEventCodes.add("Compare Requested");
      allEventCodes.add("Compare Cancelled");
      allEventCodes.add("Query Node");
      allEventCodes.add("Load Configuration");
      allEventCodes.add("Synchronize Triggers");
      allEventCodes.add("Synchronize Triggers for Table");
   }
}
