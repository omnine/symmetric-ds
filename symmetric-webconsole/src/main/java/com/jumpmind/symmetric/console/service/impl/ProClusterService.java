package com.jumpmind.symmetric.console.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.time.DateUtils;
import org.jumpmind.db.sql.ConcurrencySqlException;
import org.jumpmind.db.sql.ISqlRowMapper;
import org.jumpmind.db.sql.Row;
import org.jumpmind.db.sql.UniqueKeyException;
import org.jumpmind.symmetric.db.ISymmetricDialect;
import org.jumpmind.symmetric.model.Lock;
import org.jumpmind.symmetric.service.IExtensionService;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.service.IParameterService;
import org.jumpmind.symmetric.service.impl.ClusterService;
import org.jumpmind.util.AppUtils;

public class ProClusterService extends ClusterService {
   public ProClusterService(IParameterService parameterService, ISymmetricDialect dialect, INodeService nodeService, IExtensionService extensionService) {
      super(parameterService, dialect, nodeService, extensionService);
      this.setSqlMap(new ProClusterServiceSqlMap(this.symmetricDialect.getPlatform(), this.createSqlReplacementTokens()));
   }

   public void init() {
      super.init();
      if (this.isClusteringEnabled()) {
         log.info("Cluster locking is enabled");
         this.sqlTemplate.update(this.getSql(new String[]{"initLockSql"}), new Object[]{this.getServerId()});
         this.refreshLockEntries();
      }
   }

   public void refreshLockEntries() {
      if (this.isClusteringEnabled()) {
         Map<String, Lock> allLocks = this.findLocks();

         for (String action : actions) {
            if (allLocks.get(action) == null) {
               this.initLockTable(action, "CLUSTER");
            }
         }

         for (String actionx : sharedActions) {
            if (allLocks.get(actionx) == null) {
               this.initLockTable(actionx, "SHARED");
            }
         }
      } else {
         super.refreshLockEntries();
      }
   }

   protected void initLockTable(String action, String lockType) {
      try {
         this.sqlTemplate.update(this.getSql(new String[]{"insertLockSql"}), new Object[]{action, lockType});
         log.debug("Inserted into the LOCK table for {}, {}", action, lockType);
      } catch (UniqueKeyException var4) {
         log.debug("Failed to insert to the LOCK table for {}, {}.  Must be initialized already.", action, lockType);
      }
   }

   protected void checkSymDbOwnership() {
      if (!this.isClusteringEnabled()) {
         super.checkSymDbOwnership();
      }
   }

   public void addLock(String action, String lockType) {
      if (this.isClusteringEnabled()) {
         this.initLockTable(action, lockType);
      } else {
         super.addLock(action, lockType);
      }
   }

   public void removeLock(String action) {
      if (this.isClusteringEnabled()) {
         this.sqlTemplate.update(this.getSql(new String[]{"deleteLockSql"}), new Object[]{action});
      } else {
         super.removeLock(action);
      }
   }

   public void clearAllLocks() {
      if (this.isClusteringEnabled()) {
         this.sqlTemplate.update(this.getSql(new String[]{"initLockSql"}), new Object[]{this.getServerId()});
      } else {
         super.clearAllLocks();
      }
   }

   public synchronized void persistToTableForSnapshot() {
   }

   protected boolean lockCluster(String action, Date timeToBreakLock, Date timeLockAcquired, String argServerId) {
      if (this.isClusteringEnabled()) {
         try {
            boolean lockAcquired = this.sqlTemplate
                  .update(
                     this.getSql(new String[]{"acquireClusterLockSql"}),
                     new Object[]{argServerId, timeLockAcquired, action, "CLUSTER", timeToBreakLock, argServerId}
                  )
               == 1;
            if (lockAcquired) {
               this.updateCacheLockTime(action, timeLockAcquired);
            }

            return lockAcquired;
         } catch (ConcurrencySqlException var6) {
            log.debug("Ignoring concurrency error and reporting that we failed to get the cluster lock: {}", var6.getMessage());
            return false;
         }
      } else {
         return super.lockCluster(action, timeToBreakLock, timeLockAcquired, argServerId);
      }
   }

   protected boolean lockShared(String action) {
      if (this.isClusteringEnabled()) {
         Date timeout = DateUtils.addMilliseconds(new Date(), (int)(-this.parameterService.getLong("lock.timeout.ms")));
         return this.sqlTemplate
               .update(this.getSql(new String[]{"acquireSharedLockSql"}), new Object[]{"SHARED", this.getServerId(), new Date(), action, "SHARED", timeout})
            == 1;
      } else {
         return super.lockShared(action);
      }
   }

   protected boolean lockExclusive(String action) {
      if (this.isClusteringEnabled()) {
         Date timeout = DateUtils.addMilliseconds(new Date(), (int)(-this.parameterService.getLong("lock.timeout.ms")));
         return this.sqlTemplate
               .update(
                  this.getSql(new String[]{"acquireExclusiveLockSql"}), new Object[]{"EXCLUSIVE", this.getServerId(), new Date(), action, "SHARED", timeout}
               )
            == 1;
      } else {
         return super.lockExclusive(action);
      }
   }

   protected void disableSharedLock(String action) {
      if (this.isClusteringEnabled()) {
         this.sqlTemplate.update(this.getSql(new String[]{"disableSharedLockSql"}), new Object[]{action, "SHARED"});
      } else {
         super.disableSharedLock(action);
      }
   }

   public Map<String, Lock> findLocks() {
      if (this.isClusteringEnabled()) {
         final Map<String, Lock> locks = new HashMap<>();
         this.sqlTemplate.query(this.getSql(new String[]{"findLocksSql"}), new ISqlRowMapper<Lock>() {
            public Lock mapRow(Row rs) {
               Lock lock = new Lock();
               lock.setLockAction(rs.getString("lock_action"));
               lock.setLockType(rs.getString("lock_type"));
               lock.setLockingServerId(rs.getString("locking_server_id"));
               lock.setLockTime(rs.getDateTime("lock_time"));
               lock.setSharedCount(rs.getInt("shared_count"));
               lock.setSharedEnable(rs.getBoolean("shared_enable"));
               lock.setLastLockingServerId(rs.getString("last_locking_server_id"));
               lock.setLastLockTime(rs.getDateTime("last_lock_time"));
               locks.put(lock.getLockAction(), lock);
               return lock;
            }
         }, new Object[0]);
         return locks;
      } else {
         return super.findLocks();
      }
   }

   protected boolean unlockCluster(String action, String argServerId) {
      if (this.isClusteringEnabled()) {
         this.updateCacheLockTime(action, null);
         return this.sqlTemplate.update(this.getSql(new String[]{"releaseClusterLockSql"}), new Object[]{action, "CLUSTER", argServerId}) > 0;
      } else {
         return super.unlockCluster(action, argServerId);
      }
   }

   protected boolean unlockShared(String action) {
      return this.isClusteringEnabled()
         ? this.sqlTemplate.update(this.getSql(new String[]{"releaseSharedLockSql"}), new Object[]{action, "SHARED"}) == 1
         : super.unlockShared(action);
   }

   protected boolean unlockExclusive(String action) {
      return this.isClusteringEnabled()
         ? this.sqlTemplate.update(this.getSql(new String[]{"releaseExclusiveLockSql"}), new Object[]{action, "EXCLUSIVE"}) == 1
         : super.unlockExclusive(action);
   }

   public void aquireInfiniteLock(String action) {
      if (this.isClusteringEnabled()) {
         int tries = 600;
         Date futureTime = DateUtils.addYears(new Date(), 100);

         while (tries > 0) {
            if (!this.lockCluster(action, new Date(), futureTime, "STOPPED")) {
               AppUtils.sleep(50L);
               tries--;
            } else {
               tries = 0;
            }
         }
      } else {
         super.aquireInfiniteLock(action);
      }
   }

   public void clearInfiniteLock(String action) {
      if (this.isClusteringEnabled()) {
         Map<String, Lock> all = this.findLocks();
         Lock lock = all.get(action);
         if (lock != null && "STOPPED".equals(lock.getLockingServerId())) {
            this.sqlTemplate.update(this.getSql(new String[]{"resetClusterLockSql"}), new Object[]{action, "CLUSTER", "STOPPED"});
         }
      } else {
         super.clearInfiniteLock(action);
      }
   }

   public boolean refreshLock(String action) {
      if (this.isClusteringEnabled()) {
         Lock lock = (Lock)this.lockCache.get(action);
         long clusterLockRefreshMs = this.parameterService.getLong("cluster.lock.refresh.ms");
         long refreshTime = new Date().getTime() - clusterLockRefreshMs;
         return lock != null && lock.getLockTime() != null && lock.getLockTime().getTime() < refreshTime ? this.lock(action) : true;
      } else {
         return super.refreshLock(action);
      }
   }

   public boolean isClusteringEnabled() {
      return this.parameterService.is("cluster.lock.enabled");
   }
}
