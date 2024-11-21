package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.db.mysql.MySqlSymmetricDialect;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.web.ServerSymmetricEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqlModeMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private Logger a = LoggerFactory.getLogger(this.getClass());
   private ISymmetricEngine b;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      if (this.b instanceof ServerSymmetricEngine) {
         Collection<ServerSymmetricEngine> engines = ((ServerSymmetricEngine)this.b).getEngineHolder().getEngines().values();
         ISymmetricEngine myEngine = null;
         String myMode = null;

         for (ISymmetricEngine engine : engines) {
            if (engine.getTargetDialect() instanceof MySqlSymmetricDialect) {
               myEngine = engine;
               myMode = engine.getTargetDialect().getPlatform().getSqlTemplate().queryForString("select @@sql_mode", new Object[0]);
               break;
            }
         }

         if (myEngine != null) {
            boolean hasNonMySql = false;

            for (ISymmetricEngine enginex : engines) {
               if (enginex != myEngine && !(enginex.getTargetDialect() instanceof MySqlSymmetricDialect)) {
                  hasNonMySql = true;
                  break;
               }
            }

            Map<String, Set<String>> differentSqlModeMap = new HashMap<>();
            boolean allowsZeroDates = false;
            boolean allowsInvalidDates = false;

            for (ISymmetricEngine enginexx : engines) {
               if (enginexx != myEngine && enginexx.getTargetDialect() instanceof MySqlSymmetricDialect) {
                  String mode = enginexx.getTargetDialect().getPlatform().getSqlTemplate().queryForString("select @@sql_mode", new Object[0]);

                  for (String key : new String[]{
                     "PAD_CHAR_TO_FULL_LENGTH", "TIME_TRUNCATE_FRACTIONAL", "ALLOW_INVALID_DATES", "NO_AUTO_VALUE_ON_ZERO", "NO_ZERO_DATE", "NO_ZERO_IN_DATE"
                  }) {
                     if (myMode.contains(key) != mode.contains(key)) {
                        Set<String> engineNameSet = differentSqlModeMap.get(key);
                        if (engineNameSet == null) {
                           engineNameSet = new HashSet<>();
                        }

                        engineNameSet.add(myEngine.getEngineName() + " and " + enginexx.getEngineName());
                        differentSqlModeMap.put(key, engineNameSet);
                     }
                  }

                  if (hasNonMySql && (!allowsZeroDates || !allowsInvalidDates)) {
                     String dbUrlParamName = "db.url";
                     if (enginexx.getParameterService().is("load.only")) {
                        dbUrlParamName = "target." + dbUrlParamName;
                     }

                     String dbUrl = enginexx.getParameterService().getString(dbUrlParamName);
                     boolean hasZeroDates = !mode.contains("STRICT") && !mode.contains("NO_ZERO_DATE");
                     if (hasZeroDates && (!dbUrl.contains("zeroDateTimeBehavior") || !dbUrl.contains("convertToNull"))) {
                        allowsZeroDates = true;
                     }

                     if (!mode.contains("STRICT") && (mode.contains("ALLOW_INVALID_DATES") || !mode.contains("NO_ZERO_IN_DATE"))) {
                        allowsInvalidDates = true;
                     }
                  }
               }
            }

            if (differentSqlModeMap.isEmpty() && !allowsZeroDates && !allowsInvalidDates) {
               event.setValue(0L);
            } else {
               event.setValue(1L);
               String problemDescription = "";
               String actionDescription = "";
               if (!differentSqlModeMap.isEmpty()) {
                  for (Entry<String, Set<String>> sqlModeEntry : differentSqlModeMap.entrySet()) {
                     String sqlMode = sqlModeEntry.getKey();

                     for (String engineNamePair : sqlModeEntry.getValue()) {
                        String[] engineNames = engineNamePair.split(" and ");
                        if (engineNames.length == 2) {
                           problemDescription = problemDescription
                              + " Database for "
                              + engineNames[0]
                              + " and database for "
                              + engineNames[1]
                              + " have different SQL modes for "
                              + sqlMode
                              + ".";
                        }
                     }
                  }

                  problemDescription = problemDescription + " The SQL modes should match or there may be replication errors.";
                  actionDescription = "The database adminstrator should set the SQL modes to match between all MySQL databases using the \"SET GLOBAL sql_mode\" statement.";
               }

               if (allowsZeroDates) {
                  problemDescription = problemDescription + " Database allows zero dates (0000-00-00) which are not compatible with non-MySQL databases.";
                  actionDescription = actionDescription
                     + " Add URL parameter zeroDateTimeBehavior=convertToNull to db.url engine parameter to convert zero dates to null.";
               }

               if (allowsInvalidDates) {
                  problemDescription = problemDescription
                     + " Database allows zero in dates for month, day, or year, which is not compatible with non-MySQL databases.";
                  actionDescription = actionDescription + " Use valid dates in the source database or use transforms to convert them to valid dates.";
               }

               problemDescription = problemDescription.trim();
               actionDescription = actionDescription.trim();
               event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(new Recommendation(problemDescription, actionDescription, false)));
            }
         } else {
            event.setValue(0L);
         }
      } else {
         event.setValue(0L);
         this.a.error("Failed to get engines when checking MySQL mode insight");
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      return true;
   }

   @Override
   public String b() {
      return "mySqlMode";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.b = engine;
   }
}
