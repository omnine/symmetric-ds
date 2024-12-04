package com.jumpmind.symmetric.console.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jumpmind.db.model.Database;
import org.jumpmind.db.model.ForeignKey;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.model.Trigger;
import org.jumpmind.symmetric.service.ITriggerRouterService;

public class E {
   public static Table[] a(ISymmetricEngine engine) {
      IDatabasePlatform platform = engine.getSymmetricDialect().getPlatform();
      Database db = platform.readDatabase(null, null, null);
      Table[] tables = db.getTables();
      List<Table> tableList = new ArrayList<>();

      for (Table table : tables) {
         if (!table.getName().toLowerCase().startsWith(engine.getParameterService().getTablePrefix().toLowerCase())) {
            tableList.add(table);
         }
      }

      return tableList.toArray(new Table[tableList.size()]);
   }

   public static List<E.a> b(ISymmetricEngine engine) {
      List<E.a> auditList = new ArrayList<>();
      Table[] tables = a(engine);
      ITriggerRouterService triggerRouterService = engine.getTriggerRouterService();
      List<Trigger> triggers = triggerRouterService.getTriggers();
      List<List<Table>> segments = a(tables);
      auditList.addAll(a(tables, segments, triggers));
      return auditList;
   }

   private static List<E.a> a(Table[] tables, List<List<Table>> segments, List<Trigger> triggers) {
      List<E.a> auditList = new ArrayList<>();
      if (tables != null) {
         for (Table table : tables) {
            for (ForeignKey fk : table.getForeignKeys()) {
               Table ftable = fk.getForeignTable();
               String channelId = null;

               for (Trigger t : triggers) {
                  if (t.getSourceTableName().equals(table.getName()) || t.getSourceTableName().equals(ftable.getName())) {
                     if (channelId == null) {
                        channelId = t.getChannelId();
                     } else if (!channelId.equals(t.getChannelId())) {
                        auditList.add(
                           new E.a(
                              "Warn",
                              "Trigger Channel FK Warning",
                              String.format("Triggers %s and %s are related but assigned separate channels.", channelId, t.getChannelId())
                           )
                        );
                     }
                  }
               }
            }
         }
      }

      return auditList;
   }

   public static List<List<Table>> a(Table... tables) {
      List<List<Table>> segmentationList = null;
      if (tables != null) {
         Map<String, List<Table>> bigraph = new HashMap<>();
         List<String> visited = new ArrayList<>(tables.length);
         segmentationList = new ArrayList<>();

         for (Table table : tables) {
            List<Table> link = new ArrayList<>();

            for (ForeignKey fk : table.getForeignKeys()) {
               link.add(fk.getForeignTable());
            }

            bigraph.put(table.getName(), link);
         }

         for (Table table : tables) {
            for (ForeignKey fk : table.getForeignKeys()) {
               List<Table> fTblList = bigraph.get(fk.getForeignTableName());
               if (fTblList != null && !fTblList.contains(table)) {
                  fTblList.add(table);
               }
            }
         }

         for (Table table : tables) {
            if (!visited.contains(table.getName())) {
               List<Table> segment = new ArrayList<>();
               a(table, bigraph, visited, segment);
               segmentationList.add(segment);
            }
         }
      }

      return segmentationList;
   }

   private static void a(Table table, Map<String, List<Table>> bigraph, List<String> visited, List<Table> segment) {
      visited.add(table.getName());
      segment.add(table);

      for (Table foreignTable : bigraph.get(table.getName())) {
         if (!visited.contains(foreignTable.getName())) {
            a(foreignTable, bigraph, visited, segment);
         }
      }
   }

   public static class a {
      String a;
      String b;
      String c;

      public a(String severity, String type, String description) {
         this.a = severity;
         this.b = type;
         this.c = description;
      }

      public String a() {
         return this.a;
      }

      public void a(String severity) {
         this.a = severity;
      }

      public String b() {
         return this.b;
      }

      public void b(String type) {
         this.b = type;
      }

      public String c() {
         return this.c;
      }

      public void c(String description) {
         this.c = description;
      }
   }
}
