package com.jumpmind.symmetric.statistic;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jumpmind.symmetric.statistic.AbstractNodeHostStats;

public class TableStats extends AbstractNodeHostStats {
   private Map<String, Map<String, TableStat>> tableCounts = new ConcurrentHashMap<>();

   public TableStats() {
   }

   public TableStats(Date startTime) {
      super(null, null, startTime, null);
   }

   public Map<String, Map<String, TableStat>> getTableCounts() {
      return this.tableCounts;
   }

   public void setTableCounts(Map<String, Map<String, TableStat>> tableCounts) {
      this.tableCounts = tableCounts;
   }
}
