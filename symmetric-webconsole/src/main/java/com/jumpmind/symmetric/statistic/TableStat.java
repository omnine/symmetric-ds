package com.jumpmind.symmetric.statistic;

import org.jumpmind.symmetric.statistic.AbstractNodeHostStats;

public class TableStat extends AbstractNodeHostStats {
   private Long extractedCount;
   private Long loadedCount;

   public TableStat() {
   }

   public TableStat(Long extractedCount, Long loadedCount) {
      this.extractedCount = extractedCount;
      this.loadedCount = loadedCount;
   }

   public Long getExtractedCount() {
      return this.extractedCount;
   }

   public void setExtractedCount(Long extractedCount) {
      this.extractedCount = extractedCount;
   }

   public Long getLoadedCount() {
      return this.loadedCount;
   }

   public void setLoadedCount(Long loadedCount) {
      this.loadedCount = loadedCount;
   }

   public void incrementLoadedCount(Long loadedCount) {
      this.loadedCount = this.loadedCount + loadedCount;
   }

   public void incrementExtractedCount(Long extractedCount) {
      this.extractedCount = this.extractedCount + extractedCount;
   }
}
