package com.jumpmind.symmetric.console.ui.data;
public class MixedIncomingStatus {
   public long a;
   public String b;
   public long c;
   public long d;
   public String e;

   public MixedIncomingStatus(long batchId, String status, long processedRowCount, long totalRowCount) {
      this(batchId, status, processedRowCount, totalRowCount, null);
   }

   public MixedIncomingStatus(long batchId, String status, long processedRowCount, long totalRowCount, String sqlMessage) {
      this.a = batchId;
      this.b = status;
      this.c = processedRowCount;
      this.e = sqlMessage;
      this.d = totalRowCount;
   }
}