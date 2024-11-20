package com.jumpmind.symmetric.console.service;

//import com.jumpmind.symmetric.console.impl.fU;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.model.Notification;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public interface IMonitorService extends IBuiltInExtensionPoint {
   void update();

   List<Monitor> getMonitors();

   List<Monitor> getActiveMonitorsForNode(String var1, String var2);

   List<Monitor> getActiveMonitorsForNodeFromDb(String var1, String var2);

   void deleteMonitor(String var1);

   void saveMonitor(Monitor var1);

   void saveMonitorAsCopy(Monitor var1);

   void renameMonitor(String var1, Monitor var2);

   List<MonitorEvent> getMonitorEvents();

   List<MonitorEvent> getMonitorEventsFiltered(int var1, String var2, int var3, String var4, Boolean var5);

   List<MonitorEvent> getMonitorEventsByMonitorId(String var1);

   void saveMonitorEvent(MonitorEvent var1);

   void deleteMonitorEvent(MonitorEvent var1);

   void updateMonitorEventAsResolved(MonitorEvent var1);

//   fU getRecommendations(boolean var1);

   Map<String, Object> getRecommendationDetails(String var1, String var2, Date var3);

   void approveRecommendation(String var1, String var2, Date var3, int var4, String var5);

   void dismissRecommendation(String var1, String var2, Date var3, long var4);

   void undoDismissalForRecommendation(String var1, String var2, Date var3);

   List<Notification> getNotifications();

   List<Notification> getActiveNotificationsForNode(String var1, String var2);

   List<Notification> getActiveNotificationsForNodeFromDb(String var1, String var2);

   void saveNotification(Notification var1);

   void saveNotificationAsCopy(Notification var1);

   void renameNotification(String var1, Notification var2);

   void deleteNotification(String var1);

   void flushMonitorCache();

   void flushNotificationCache();

   List<Monitor> getActiveMonitorsUnresolvedForNode(String var1, String var2);

   List<Monitor> getActiveMonitorsUnresolvedForNodeFromDb(String var1, String var2);
}
