package com.jumpmind.symmetric.notification;

import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.model.Notification;
import java.util.List;
import java.util.Map;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.util.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerNotification implements INotificationExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private final Logger b = LoggerFactory.getLogger(this.getClass());
   protected ISymmetricEngine a;

   @Override
   public void output(Notification notification, List<MonitorEvent> monitorEvents) {
      Notification.LogExpression expression = notification.getLogExpression();

      for (MonitorEvent monitorEvent : monitorEvents) {
         Map<String, String> replacements = NotificationTemplate.a(this.a, monitorEvent);
         String message;
         if (monitorEvent.isResolved()) {
            message = FormatUtils.replaceTokens(expression.getResolved(), replacements, true);
         } else {
            message = FormatUtils.replaceTokens(expression.getUnresolved(), replacements, true);
         }

         if (monitorEvent.getSeverityLevel() >= 300) {
            this.b.error(message);
         } else if (monitorEvent.getSeverityLevel() >= 200) {
            this.b.warn(message);
         } else {
            this.b.info(message);
         }
      }
   }

   @Override
   public String channel() {
      return "log";
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
