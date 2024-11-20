package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.db.model.Transaction;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.util.SymmetricUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private final Logger a = LoggerFactory.getLogger(
      new ge(
            new long[]{
               6153412712315247243L,
               -6348623684102535553L,
               3909780065137416991L,
               6087296959336269442L,
               -2148750679028070869L,
               -2869448436738399951L,
               3018127492706261409L,
               -5901248536200439031L,
               -4028198908276114537L
            }
         )
         .toString()
   );
   private ISymmetricEngine b;
   private boolean c = true;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<Transaction> transactions = null;
      if (this.c) {
         try {
            transactions = this.b.getTargetDialect().getTargetPlatform().getTransactions();
         } catch (RuntimeException var12) {
            this.a.info("Disabling blocked transaction monitor because {}: {}", var12.getClass().getName(), var12.getMessage());
            this.c = false;
         }
      }

      if (transactions != null && !transactions.isEmpty()) {
         Map<String, Transaction> transactionMap = new HashMap<>();

         for (Transaction transaction : transactions) {
            String id = transaction.getId();
            if (!transactionMap.containsKey(id) || transactionMap.get(transaction.getBlockingId()) == null) {
               transactionMap.put(id, transaction);
            }
         }

         List<Transaction> filteredTransactions = new ArrayList<>();
         String dbUser = this.b.getParameterService().getString("db.user");

         for (Transaction transactionx : transactions) {
            SymmetricUtils.filterTransactions(transactionx, transactionMap, filteredTransactions, dbUser, false, false);
         }

         long secondsBlocked = 0L;

         for (Transaction transactionx : filteredTransactions) {
            if (StringUtils.equals(transactionx.getUsername(), dbUser) && transactionMap.get(transactionx.getBlockingId()) != null) {
               secondsBlocked = Math.max(secondsBlocked, transactionx.getDuration() / 1000L);
            }
         }

         event.setValue(secondsBlocked);
         if (secondsBlocked > 0L) {
            String problemDescription = "A transaction has been blocked for " + secondsBlocked + " seconds.";
            String actionDescription = "Ask the user of blocking transactions to commit or rollback, or ask the database administrator to kill the transactions.";
            fT recommendation = new fT(problemDescription, actionDescription, false);
            recommendation.a("transactionList", com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(transactions));
            event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
         }
      } else {
         event.setValue(0L);
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, fT recommendation) {
      return true;
   }

   @Override
   public String b() {
      return "block";
   }

   @Override
   public boolean a() {
      return false;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.b = engine;
   }
}
