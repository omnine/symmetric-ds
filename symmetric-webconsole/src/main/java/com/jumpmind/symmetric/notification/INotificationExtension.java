package com.jumpmind.symmetric.notification;

import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.model.Notification;
import java.util.List;
import org.jumpmind.extension.IExtensionPoint;

public interface INotificationExtension extends IExtensionPoint {
   void output(Notification var1, List<MonitorEvent> var2); //save to log file or send email

   String channel();
}
