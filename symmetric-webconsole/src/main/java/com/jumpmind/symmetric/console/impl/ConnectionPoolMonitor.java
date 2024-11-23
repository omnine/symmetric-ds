package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbcp2.BasicDataSource;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.properties.TypedProperties;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ITypedPropertiesFactory;
import org.jumpmind.symmetric.db.ISymmetricDialect;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionPoolMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;
   private Logger b = LoggerFactory.getLogger(this.getClass());

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      ISymmetricDialect dialect = this.a.getSymmetricDialect();
      BasicDataSource dataSource = dialect.getTargetPlatform().getDataSource();
      boolean allConnectionsInUse = dataSource != null && dataSource.getNumActive() == dataSource.getMaxTotal();
      event.setValue(allConnectionsInUse ? 1L : 0L);
      if (allConnectionsInUse) {
         String problemDescription = "All " + dataSource.getMaxTotal() + " connections in the database connection pool are in use.";
         String actionDescription = "Increase the size of the database connection pool.";
         Recommendation recommendation = new Recommendation(problemDescription, actionDescription, true);
         List<Recommendation.a> options = new ArrayList<>();
         String optionDescription;
         if (dialect.equals(this.a.getTargetDialect())) {
            optionDescription = "Increase the db.pool.max.active parameter by 10 and restart SymmetricDS.";
         } else {
            optionDescription = "Increase the target.db.pool.max.active parameter by 10 and restart SymmetricDS.";
         }

         options.add(recommendation.new a(1, optionDescription));
         recommendation.a(options);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      String engineName = this.a.getEngineName();
      File propFile = PropertiesUtil.findPropertiesFileForEngineWithName(engineName);
      if (propFile == null) {
         this.b.error("Failed to open or find properties file for " + engineName);
         return false;
      } else if (!propFile.canWrite()) {
         this.b.error("Failed to write to properties file for " + engineName);
         return false;
      } else {
         String selectedOptionDescription = recommendation.b(event.getApprovedOption());
         String parameterName = "db.pool.max.active";
         if (selectedOptionDescription != null && selectedOptionDescription.contains("target.")) {
            parameterName = "target." + parameterName;
         }

         try {
            ITypedPropertiesFactory typedPropertiesFactory = PropertiesUtil.createTypedPropertiesFactory(propFile, null);
            TypedProperties prop = typedPropertiesFactory.reload(propFile);
            int currentValue = Integer.parseInt(prop.get(parameterName, "50"));
            prop.put(parameterName, String.valueOf(currentValue + 10));
            typedPropertiesFactory.save(prop, propFile, "Updated by SymmetricDS");
            IConsoleEventService consoleEventService = this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);
            String nodeId = this.a.getNodeId();
            consoleEventService.addEvent(new ConsoleEvent(event.getApprovedBy(), "Edit Parameter", nodeId, nodeId, null, parameterName));
         } catch (IOException var12) {
            String msg = var12.getMessage();
            if (msg != null) {
               this.b.error(msg, var12);
            } else {
               this.b.error("Failed to read properties file for " + engineName, var12);
            }

            return false;
         }

         System.exit(0);
         return true;
      }
   }

   @Override
   public String b() {
      return "connectionPool";
   }

   @Override
   public boolean a() {
      return false;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
