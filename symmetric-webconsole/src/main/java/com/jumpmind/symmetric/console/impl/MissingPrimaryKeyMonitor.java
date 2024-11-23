package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jumpmind.db.model.CatalogSchema;
import org.jumpmind.db.model.Table;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.util.SnapshotUtil;

public class MissingPrimaryKeyMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      HashMap<CatalogSchema, List<Table>> tablesMap = SnapshotUtil.getTablesForCaptureByCatalogSchema(this.a);
      Set<String> tablesMissingPkSet = new HashSet<>();

      for (List<Table> tableList : tablesMap.values()) {
         for (Table table : tableList) {
            if (table.getPrimaryKeyColumnCount() == 0 && table.getUniqueIndices().length == 0) {
               tablesMissingPkSet.add(table.getQualifiedTableName());
            }
         }
      }

      event.setValue(tablesMissingPkSet.size());
      if (tablesMissingPkSet.size() > 0) {
         if (tablesMissingPkSet.size() == 1) {
            String problemDescription = "The "
               + tablesMissingPkSet.iterator().next()
               + " table is missing a primary key or unique index, which can cause slow updates and duplicate rows.";
            String actionDescription = "Add a primary key or unique index to the table and rebuild triggers.";
            event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(new Recommendation(problemDescription, actionDescription, false)));
         } else {
            String problemDescription = "The following tables are missing a primary key or unique index, which can cause slow updates and duplicate rows: "
               + tablesMissingPkSet;
            String actionDescription = "Add a primary key or unique index to the tables and rebuild triggers.";
            event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(new Recommendation(problemDescription, actionDescription, false)));
         }
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      return true;
   }

   @Override
   public String b() {
      return "missingPrimaryKey";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
