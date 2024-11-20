package com.jumpmind.symmetric.cache;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.Notification;
import java.util.List;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public interface IProCacheManager extends IBuiltInExtensionPoint {
   List<Monitor> getActiveMonitorsForNode(String var1, String var2);

   List<Monitor> getActiveMonitorsUnresolvedForNode(String var1, String var2);

   void flushMonitorCache();

   List<Notification> getActiveNotificationsForNode(String var1, String var2);

   void flushNotificationCache();
}
