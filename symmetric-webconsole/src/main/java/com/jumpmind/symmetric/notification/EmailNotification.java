package com.jumpmind.symmetric.notification;

import com.google.gson.reflect.TypeToken;
import com.jumpmind.symmetric.console.impl.fe;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.model.Notification;
import com.jumpmind.symmetric.console.service.IMailService;
import com.jumpmind.symmetric.console.ui.common.am;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.IncomingBatch;
import org.jumpmind.symmetric.model.OutgoingBatch;
import org.jumpmind.util.FormatUtils;
import org.jumpmind.util.LogSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotification implements INotificationExtension, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private final Logger b = LoggerFactory.getLogger(this.getClass());
   protected ISymmetricEngine a;

   @Override
   public void a(Notification notification, List<MonitorEvent> monitorEvents) {
      Map<String, String> eventListReplacements = e.a(this.a, monitorEvents);
      Notification.EmailExpression expression = notification.getEmailExpression();
      String subject = FormatUtils.replaceTokens(expression.getSubject(), eventListReplacements, true);
      Map<String, String> templateMap = expression.getTemplateMap();
      StringBuilder text = new StringBuilder();
      if (!StringUtils.isBlank(expression.getBodyBefore())) {
         text.append(FormatUtils.replaceTokens(expression.getBodyBefore(), eventListReplacements, true) + "\n");
      }

      for (MonitorEvent event : monitorEvents) {
         try {
            Map<String, String> eventReplacements = e.a(this.a, event);
            if (event.getType().equals("log")) {
               eventReplacements.put("eventDetails", c(event));
            } else if (event.getType().equals("batchError")) {
               eventReplacements.put("eventDetails", b(event));
            } else if (event.getType().equals("offlineNodes")) {
               eventReplacements.put("eventDetails", a(event));
            }

            if (monitorEvents.indexOf(event) > 0) {
               text.append("\n");
            }

            if (event.isResolved()) {
               text.append(FormatUtils.replaceTokens(expression.getResolved(), eventReplacements, true));
            } else {
               text.append(FormatUtils.replaceTokens(expression.getUnresolved(), eventReplacements, true));
               String template = templateMap.get(event.getType());
               if (template == null) {
                  template = templateMap.get("default");
               }

               if (template != null) {
                  text.append("\n" + FormatUtils.replaceTokens(template, eventReplacements, true));
               }
            }
         } catch (Exception var12) {
            this.b.debug("", var12);
         }
      }

      if (!StringUtils.isBlank(expression.getBodyAfter())) {
         text.append("\n" + FormatUtils.replaceTokens(expression.getBodyAfter(), eventListReplacements, true));
      }

      String recipients = String.join(",", expression.getEmails());
      if (recipients != null) {
         this.b.info("Sending email with subject '" + subject + "' to " + recipients);
         ((IMailService)this.a.getExtensionService().getExtensionPoint(IMailService.class)).sendEmail(subject, text.toString(), recipients);
      } else {
         this.b.warn("Notification " + notification.getNotificationId() + " has no email recipients configured.");
      }
   }

   protected static String a(MonitorEvent event) throws IOException {
      StringBuilder stackTrace = new StringBuilder();
      stackTrace.append("\n");

      for (String node : d(event)) {
         stackTrace.append("Node ").append(node).append(" is offline.").append("\n");
      }

      return stackTrace.toString();
   }

   protected static String b(MonitorEvent event) throws IOException {
      StringBuilder stackTrace = new StringBuilder();
      fe errors = e(event);
      if (errors != null) {
         for (OutgoingBatch b : errors.a()) {
            stackTrace.append("The outgoing batch ").append(b.getNodeBatchId());
            stackTrace.append(" failed: ").append(b.getSqlMessage()).append("\n");
         }

         for (IncomingBatch b : errors.b()) {
            stackTrace.append("The incoming batch ").append(b.getNodeBatchId());
            stackTrace.append(" failed: ").append(b.getSqlMessage()).append("\n");
         }
      }

      return stackTrace.toString();
   }

   protected static String c(MonitorEvent event) throws IOException {
      StringBuilder stackTrace = new StringBuilder();
      int count = 0;

      for (LogSummary summary : f(event)) {
         if (summary.getMessage() != null) {
            stackTrace.append(summary.getMessage());
            count++;
         }

         if (summary.getStackTrace() != null) {
            stackTrace.append(summary.getStackTrace());
            count++;
         }
      }

      if (count > 0) {
         stackTrace.append("\n");
      }

      return stackTrace.toString();
   }

   protected static List<String> d(MonitorEvent event) throws IOException {
      List<String> nodes = null;
      if (event.getDetails() != null) {
         nodes = (List<String>)am.getMonitorEventGson().fromJson(event.getDetails(), (new TypeToken<List<String>>() {
         }).getType());
      }

      if (nodes == null) {
         nodes = Collections.emptyList();
      }

      return nodes;
   }

   protected static fe e(MonitorEvent event) {
      fe batches = null;
      if (event.getDetails() != null) {
         batches = (fe)am.getMonitorEventGson().fromJson(event.getDetails(), fe.class);
      }

      return batches;
   }

   protected static List<LogSummary> f(MonitorEvent event) throws IOException {
      List<LogSummary> summaries = null;
      if (event.getDetails() != null) {
         summaries = (List<LogSummary>)am.getMonitorEventGson().fromJson(event.getDetails(), (new TypeToken<List<LogSummary>>() {
         }).getType());
      }

      if (summaries == null) {
         summaries = Collections.emptyList();
      }

      return summaries;
   }

   @Override
   public String a() {
      return "email";
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
