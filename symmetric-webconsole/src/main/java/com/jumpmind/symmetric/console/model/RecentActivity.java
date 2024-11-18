package com.jumpmind.symmetric.console.model;

//import com.jumpmind.symmetric.console.impl.bA;
import java.io.Serializable;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecentActivity implements Serializable, Comparable<RecentActivity> {
   protected final Logger log = LoggerFactory.getLogger(this.getClass());
   private static final long serialVersionUID = 1L;
   String message;
   boolean running;
   Date startTime;
   Date endTime;
   long dataCount;

   public RecentActivity(String message, boolean running, long dataCount, Date startTime, Date endTime) {
      this.message = message;
      this.dataCount = dataCount;
      this.startTime = startTime;
      this.endTime = endTime;
      this.running = running;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(String message) {
      this.message = message;
   }
/*
   format the time, ideally use locale
   public String getTime() {
      return this.endTime == null ? "-" : bA.b(this.endTime);
   }
*/
   public boolean isRunning() {
      return this.running;
   }

   public void setRunning(boolean running) {
      this.running = running;
   }

   public Date getStartTime() {
      return this.startTime;
   }

   public void setStartTime(Date startTime) {
      this.startTime = startTime;
   }

   public Date getEndTime() {
      return this.endTime;
   }

   public void setEndTime(Date endTime) {
      this.endTime = endTime;
   }

   public long getDataCount() {
      return this.dataCount;
   }

   public void setDataCount(long dataCount) {
      this.dataCount = dataCount;
   }

   public int compareTo(RecentActivity o) {
      if (this.endTime == null && o.endTime != null) {
         return -1;
      } else if (this.endTime != null && o.endTime == null) {
         return 1;
      } else {
         return this.endTime == null && o.endTime == null ? o.startTime.compareTo(this.startTime) : o.endTime.compareTo(this.endTime);
      }
   }
}
