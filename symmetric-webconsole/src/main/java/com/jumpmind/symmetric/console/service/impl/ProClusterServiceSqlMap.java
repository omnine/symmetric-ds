package com.jumpmind.symmetric.console.service.impl;

import java.util.Map;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.symmetric.service.impl.ClusterServiceSqlMap;

public class ProClusterServiceSqlMap extends ClusterServiceSqlMap {
   public ProClusterServiceSqlMap(IDatabasePlatform platform, Map<String, String> replacementTokens) {
      super(platform, replacementTokens);
      this.putSql(
         "acquireClusterLockSql",
         "update $(lock) set locking_server_id=?, lock_time=? where lock_action=? and lock_type=? and (lock_time is null or lock_time < ? or locking_server_id=?)"
      );
      this.putSql(
         "acquireSharedLockSql",
         "update $(lock) set lock_type=?, locking_server_id=?, lock_time=?, shared_enable=(case when shared_count = 0 then 1 else shared_enable end), shared_count=shared_count+1 where lock_action=? and (lock_type=? or lock_time is null or lock_time < ?) and (shared_enable = 1 or shared_count = 0)"
      );
      this.putSql("disableSharedLockSql", "update $(lock) set shared_enable=0 where lock_action=? and lock_type=?");
      this.putSql(
         "acquireExclusiveLockSql",
         "update $(lock) set lock_type=?, locking_server_id=?, lock_time=?, shared_count=0 where lock_action=? and ((lock_type=? and shared_count = 0) or lock_time is null or lock_time < ?)"
      );
      this.putSql(
         "releaseClusterLockSql",
         "update $(lock) set last_locking_server_id=locking_server_id, locking_server_id=null, last_lock_time=lock_time, lock_time=null where lock_action=? and lock_type=? and locking_server_id=?"
      );
      this.putSql(
         "resetClusterLockSql",
         "update $(lock) set last_locking_server_id=null, locking_server_id=null, last_lock_time=null, lock_time=null where lock_action=? and lock_type=? and locking_server_id=?"
      );
      this.putSql(
         "releaseSharedLockSql",
         "update $(lock) set last_lock_time=lock_time, last_locking_server_id=locking_server_id, shared_enable=(case when shared_count = 1 then 0 else shared_enable end), locking_server_id = (case when shared_count = 1 then null else locking_server_id end), lock_time = (case when shared_count = 1 then null else lock_time end), shared_count=(case when shared_count > 1 then shared_count-1 else 0 end) where lock_action=? and lock_type=?"
      );
      this.putSql(
         "releaseExclusiveLockSql",
         "update $(lock) set last_locking_server_id=locking_server_id, locking_server_id=null, last_lock_time=lock_time, lock_time=null where lock_action=? and lock_type=?"
      );
      this.putSql(
         "initLockSql",
         "update $(lock) set last_locking_server_id=locking_server_id, locking_server_id=null, last_lock_time=lock_time, lock_time=null, shared_count=0, shared_enable=0 where locking_server_id=?"
      );
      this.putSql("insertLockSql", "insert into $(lock) (lock_action, lock_type) values(?,?)");
      this.putSql("deleteLockSql", "delete from $(lock) where lock_action = ?");
      this.putSql(
         "findLocksSql",
         "select lock_action, lock_type, locking_server_id, lock_time, shared_count, shared_enable, last_locking_server_id, last_lock_time from $(lock)"
      );
   }
}
