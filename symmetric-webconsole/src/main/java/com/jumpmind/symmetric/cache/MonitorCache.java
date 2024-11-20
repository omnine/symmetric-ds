package com.jumpmind.symmetric.cache;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.Notification;
import com.jumpmind.symmetric.console.service.IMonitorService;
import java.util.List;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.service.IParameterService;

public class MonitorCache {
   private IParameterService a;
   private IMonitorService b;
   private volatile List<Monitor> c;
   private volatile long d;
   private volatile List<Monitor> e;
   private volatile long f;
   private volatile List<Notification> g;
   private volatile long h;
   private volatile Object i = new Object();

   public MonitorCache(ISymmetricEngine engine) {
      this.a = engine.getParameterService();
      this.b = (IMonitorService)engine.getExtensionService().getExtensionPoint(IMonitorService.class);
   }

   public List<Monitor> a(String nodeGroupId, String externalId) {
      long cacheTimeout = this.a.getLong("cache.monitor.time.ms");
      if (this.c == null || System.currentTimeMillis() - this.d > cacheTimeout) {
         synchronized (this.i) {
            if (this.c == null || System.currentTimeMillis() - this.d > cacheTimeout) {
               this.c = this.b.getActiveMonitorsForNodeFromDb(nodeGroupId, externalId);
               this.d = System.currentTimeMillis();
            }
         }
      }

      return this.c;
   }

   public List<Monitor> b(String nodeGroupId, String externalId) {
      long cacheTimeout = this.a.getLong("cache.monitor.time.ms");
      if (this.e == null || System.currentTimeMillis() - this.f > cacheTimeout) {
         synchronized (this.i) {
            if (this.e == null || System.currentTimeMillis() - this.f > cacheTimeout) {
               this.e = this.b.getActiveMonitorsUnresolvedForNodeFromDb(nodeGroupId, externalId);
               this.f = System.currentTimeMillis();
            }
         }
      }

      return this.e;
   }

   public void a() {
      synchronized (this.i) {
         this.d = 0L;
         this.f = 0L;
      }
   }

   public List<Notification> c(String nodeGroupId, String externalId) {
      long cacheTimeout = this.a.getLong("cache.notification.time.ms");
      if (this.g == null || System.currentTimeMillis() - this.h > cacheTimeout) {
         synchronized (this.i) {
            if (this.g == null || System.currentTimeMillis() - this.h > cacheTimeout) {
               this.g = this.b.getActiveNotificationsForNodeFromDb(nodeGroupId, externalId);
               this.h = System.currentTimeMillis();
            }
         }
      }

      return this.g;
   }

   public void b() {
      synchronized (this.i) {
         this.h = 0L;
      }
   }
}
