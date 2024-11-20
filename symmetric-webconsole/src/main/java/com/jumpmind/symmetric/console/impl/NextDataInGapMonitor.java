package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import java.util.ArrayList;
import java.util.List;
import org.jumpmind.db.platform.oracle.OracleDatabasePlatform;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.DataGap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NextDataInGapMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;
   private Logger b = LoggerFactory.getLogger(this.getClass());

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      long nextDataId = this.a.getDataService().findMaxDataId() + 1L;
      List<DataGap> dataGaps = this.a.getRouterService().getDataGaps();
      if (dataGaps != null && nextDataId > 1L) {
         boolean foundInGap = this.a(this.a.getRouterService().getDataGaps(), nextDataId);
         if (!foundInGap && this.a.getDatabasePlatform() instanceof OracleDatabasePlatform) {
            foundInGap = this.a(this.a.getRouterService().getDataGaps(), nextDataId + 1000L);
         }

         if (!foundInGap) {
            event.setValue(1L);
            String dataGapTableName = this.a.getParameterService().getTablePrefix() + "_data_gap";
            String problemDescription = "Next data ID of "
               + nextDataId
               + " is outside of any data gap.  This can happen if the data ID sequence experiences a large increment.";
            String actionDescription = "Manually repair "
               + dataGapTableName
               + ".  In an emergency, stop the service, clear "
               + dataGapTableName
               + ", and restart.";
            fT recommendation = new fT(problemDescription, actionDescription, true);
            List<fT.a> options = new ArrayList<>();
            options.add(recommendation.new a(1, "Repair " + dataGapTableName));
            recommendation.a(options);
            event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
         } else {
            event.setValue(0L);
         }
      } else {
         event.setValue(0L);
      }

      return event;
   }

   protected boolean a(List<DataGap> gaps, long nextDataId) {
      boolean foundInGap = false;

      for (DataGap dataGap : gaps) {
         if (nextDataId >= dataGap.getStartId() && nextDataId <= dataGap.getEndId()) {
            foundInGap = true;
            break;
         }
      }

      return foundInGap;
   }

   @Override
   public boolean a(MonitorEvent event, fT recommendation) {
      if (this.a.getClusterService().lock("Routing")) {
         this.a.getDataService().fixLastDataGap();
         return true;
      } else {
         this.b.error("Failed to approve Next Data in Gap insight. Could not acquire routing cluster lock.");
         return false;
      }
   }

   @Override
   public String b() {
      return "nextDataInGap";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
