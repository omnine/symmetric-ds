package com.jumpmind.symmetric.cache;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.Notification;
import java.util.List;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;

public class ProCacheManager implements IProCacheManager, ISymmetricEngineAware {
   private ISymmetricEngine engine;
   private volatile Object constructorCreator = new Object();
   private volatile MonitorCache monitorCache;

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.engine = engine;
   }

   private void initializeMonitorCache() {
      if (this.monitorCache == null) {
         synchronized (this.constructorCreator) {
            if (this.monitorCache == null) {
               this.monitorCache = new MonitorCache(this.engine);
            }
         }
      }
   }

   @Override
   public List<Monitor> getActiveMonitorsForNode(String nodeGroupId, String externalId) {
      this.initializeMonitorCache();
      return this.monitorCache.a(nodeGroupId, externalId);
   }

   @Override
   public List<Monitor> getActiveMonitorsUnresolvedForNode(String nodeGroupId, String externalId) {
      this.initializeMonitorCache();
      return this.monitorCache.b(nodeGroupId, externalId);
   }

   @Override
   public List<Notification> getActiveNotificationsForNode(String nodeGroupId, String externalId) {
      this.initializeMonitorCache();
      return this.monitorCache.c(nodeGroupId, externalId);
   }

   @Override
   public void flushMonitorCache() {
      this.initializeMonitorCache();
      this.monitorCache.a();
   }

   @Override
   public void flushNotificationCache() {
      this.initializeMonitorCache();
      this.monitorCache.b();
   }
}
