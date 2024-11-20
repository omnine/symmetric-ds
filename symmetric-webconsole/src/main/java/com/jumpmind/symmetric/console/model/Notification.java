package com.jumpmind.symmetric.console.model;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.symmetric.model.IModelObject;

public class Notification implements IModelObject {
   private static final long serialVersionUID = 1L;
   private String notificationId;
   private String nodeGroupId;
   private String externalId;
   private int severityLevel;
   private String type;
   private String expression;
   private boolean enabled;
   private Date createTime;
   private String lastUpdateBy;
   private Date lastUpdateTime;
   private transient String targetNode;

   public String getNotificationId() {
      return this.notificationId;
   }

   public void setNotificationId(String notificationId) {
      this.notificationId = notificationId;
   }

   public String getExternalId() {
      return this.externalId;
   }

   public void setExternalId(String externalId) {
      this.externalId = externalId;
   }

   public String getNodeGroupId() {
      return this.nodeGroupId;
   }

   public void setNodeGroupId(String nodeGroupId) {
      this.nodeGroupId = nodeGroupId;
   }

   public int getSeverityLevel() {
      return this.severityLevel;
   }

   public void setSeverityLevel(int severityLevel) {
      this.severityLevel = severityLevel;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public Date getCreateTime() {
      return this.createTime;
   }

   public void setCreateTime(Date createTime) {
      this.createTime = createTime;
   }

   public String getLastUpdateBy() {
      return this.lastUpdateBy;
   }

   public void setLastUpdateBy(String lastUpdateBy) {
      this.lastUpdateBy = lastUpdateBy;
   }

   public Date getLastUpdateTime() {
      return this.lastUpdateTime;
   }

   public void setLastUpdateTime(Date lastUpdateTime) {
      this.lastUpdateTime = lastUpdateTime;
   }

   public String getExpression() {
      return this.expression;
   }

   public Notification.LogExpression getLogExpression() {
      return this.type != null && this.type.equals("log") && this.expression != null && this.expression.contains("{")
         ? (Notification.LogExpression)new Gson().fromJson(this.expression, Notification.LogExpression.class)
         : new Notification.LogExpression();
   }

   public Notification.EmailExpression getEmailExpression() {
      if (this.type != null && this.type.equals("email") && this.expression != null && this.expression.contains("{")) {
         return (Notification.EmailExpression)new Gson().fromJson(this.expression, Notification.EmailExpression.class);
      } else {
         Notification.EmailExpression emailExpression = new Notification.EmailExpression();
         if (!StringUtils.isEmpty(this.expression)) {
            emailExpression.setEmails(Arrays.asList(this.expression.split(",", -1)));
         }

         return emailExpression;
      }
   }

   public void setExpression(String expression) {
      this.expression = expression;
   }

   public void setExpression(Notification.LogExpression expression) {
      this.expression = new Gson().toJson(expression);
   }

   public void setExpression(Notification.EmailExpression expression) {
      this.expression = new Gson().toJson(expression);
   }

   public String getSeverityLevelName() {
      String name = Monitor.severityLevelNames.get(this.severityLevel);
      if (name == null) {
         name = "INFO";
      }

      return name;
   }

   public String getTargetNode() {
      if (this.targetNode == null) {
         if (this.externalId != null && !this.externalId.equals("ALL")) {
            this.targetNode = this.externalId + " only";
         } else {
            this.targetNode = this.nodeGroupId;
         }
      }

      return this.targetNode;
   }

   public void setTargetNode(String targetNode) {
      this.targetNode = targetNode;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      return 31 * result + (this.notificationId == null ? 0 : this.notificationId.hashCode());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         Notification other = (Notification)obj;
         if (this.notificationId == null) {
            if (other.notificationId != null) {
               return false;
            }
         } else if (!this.notificationId.equals(other.notificationId)) {
            return false;
         }

         return true;
      }
   }

   public class EmailExpression {
      private List<String> emails = new ArrayList<>();
      private String subject = "Monitor events for $(eventTypes) from nodes $(eventNodeIds)";
      private String bodyBefore = "";
      private String bodyAfter = "";
      private String unresolved = "Monitor event for $(eventType) reached threshold of $(eventThreshold) with a value of $(eventValue)";
      private String resolved = "Monitor event for $(eventType) is resolved";
      private List<Notification.Template> templates = new ArrayList<>();

      public EmailExpression() {
         this.templates.add(Notification.this.new Template("log", "Details: \n$(eventDetails)"));
         this.templates.add(Notification.this.new Template("batchError", "Details: \n$(eventDetails)"));
         this.templates.add(Notification.this.new Template("offlineNodes", "Details: \n$(eventDetails)"));
         this.templates.add(Notification.this.new Template("batchUnsent", "$(eventValue) batches unsent."));
         this.templates.add(Notification.this.new Template("dataUnrouted", "$(eventValue) unrouted data rows."));
         this.templates.add(Notification.this.new Template("dataGap", "$(eventValue) data gap(s) recorded"));
         this.templates.add(Notification.this.new Template("cpu", "Details: \n$(eventDetails)"));
         this.templates.add(Notification.this.new Template("memory", "Details: \n$(eventDetails)"));
         this.templates.add(Notification.this.new Template("disk", "Disk usage is at $(eventValue)%"));
         this.templates.add(Notification.this.new Template("block", "Details: \n$(eventDetails)"));
         this.templates.add(Notification.this.new Template("default", "Details: \n$(eventDetails)"));
      }

      public List<String> getEmails() {
         return this.emails;
      }

      public void setEmails(List<String> emails) {
         this.emails = emails;
      }

      public String getSubject() {
         return this.subject;
      }

      public void setSubject(String subject) {
         this.subject = subject;
      }

      public String getBodyBefore() {
         return this.bodyBefore;
      }

      public void setBodyBefore(String bodyBefore) {
         this.bodyBefore = bodyBefore;
      }

      public String getBodyAfter() {
         return this.bodyAfter;
      }

      public void setBodyAfter(String bodyAfter) {
         this.bodyAfter = bodyAfter;
      }

      public String getUnresolved() {
         return this.unresolved;
      }

      public void setUnresolved(String unresolved) {
         this.unresolved = unresolved;
      }

      public String getResolved() {
         return this.resolved;
      }

      public void setResolved(String resolved) {
         this.resolved = resolved;
      }

      public List<Notification.Template> getTemplates() {
         return this.templates;
      }

      public Map<String, String> getTemplateMap() {
         Map<String, String> templateMap = new HashMap<>();

         for (Notification.Template template : this.templates) {
            templateMap.put(template.getName(), template.getTemplate());
         }

         return templateMap;
      }

      public void setTemplates(List<Notification.Template> templates) {
         this.templates = templates;
      }

      public void setTemplates(Map<String, String> templateMap) {
         this.templates = new ArrayList<>();

         for (String name : templateMap.keySet()) {
            this.templates.add(Notification.this.new Template(name, templateMap.get(name)));
         }
      }
   }

   public class LogExpression {
      private String unresolved = "Monitor $(eventType) on $(eventNodeId) reached threshold of $(eventThreshold) with a value of $(eventValue)";
      private String resolved = "Monitor $(eventType) on $(eventNodeId) is resolved";

      public String getUnresolved() {
         return this.unresolved;
      }

      public void setUnresolved(String unresolved) {
         this.unresolved = unresolved;
      }

      public String getResolved() {
         return this.resolved;
      }

      public void setResolved(String resolved) {
         this.resolved = resolved;
      }
   }

   public class Template {
      private String name;
      private String template;

      public Template(String name, String template) {
         this.name = name;
         this.template = template;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getTemplate() {
         return this.template;
      }

      public void setTemplate(String template) {
         this.template = template;
      }
   }
}
