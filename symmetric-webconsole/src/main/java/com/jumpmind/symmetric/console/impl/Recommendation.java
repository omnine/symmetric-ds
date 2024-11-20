package com.jumpmind.symmetric.console.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recommendation {
   String a;
   String b;
   Map<String, Object> c;
   List<Recommendation.a> d;
   boolean e;
   String f;
   String g;
   Map<String, Date> h;
   Integer i;

   public Recommendation() {
   }

   public Recommendation(String problemDescription, String actionDescription, boolean canApprove) {
      this.a = problemDescription;
      this.b = actionDescription;
      this.e = canApprove;
   }

   public String a() {
      return this.a;
   }

   public void a(String problemDescription) {
      this.a = problemDescription;
   }

   public String b() {
      return this.b;
   }

   public void b(String actionDescription) {
      this.b = actionDescription;
   }

   public Map<String, Object> c() {
      return this.c;
   }

   public Object c(String key) {
      return this.c != null ? this.c.get(key) : null;
   }

   public void a(String key, Object value) {
      if (this.c == null) {
         this.c = new HashMap<>();
      }

      this.c.put(key, value);
   }

   public List<Recommendation.a> d() {
      return this.d;
   }

   public long a(int optionId) {
      if (this.d != null) {
         for (Recommendation.a option : this.d) {
            if (option.a() == optionId) {
               return option.c();
            }
         }
      }

      return 0L;
   }

   public String b(int optionId) {
      if (this.d != null) {
         for (Recommendation.a option : this.d) {
            if (option.a() == optionId) {
               return option.b();
            }
         }
      }

      return null;
   }

   public void a(List<Recommendation.a> options) {
      this.d = options;
   }

   public boolean e() {
      return this.e;
   }

   public void a(boolean canApprove) {
      this.e = canApprove;
   }

   public String f() {
      return this.f;
   }

   public void d(String monitorId) {
      this.f = monitorId;
   }

   public String g() {
      return this.g;
   }

   public void e(String type) {
      this.g = type;
   }

   public Map<String, Date> h() {
      return this.h;
   }

   public void a(Map<String, Date> eventTimeByNodeIdMap) {
      this.h = eventTimeByNodeIdMap;
   }

   public void a(String nodeId, Date eventTime) {
      if (this.h == null) {
         this.h = new HashMap<>();
      }

      this.h.put(nodeId, eventTime);
   }

   public void f(String nodeId) {
      if (this.h != null) {
         this.h.remove(nodeId);
      }
   }

   public void i() {
      if (this.h != null) {
         this.h.clear();
      }
   }

   public Date j() {
      Date mostRecentEventTime = null;
      if (this.h != null) {
         for (Date eventTime : this.h.values()) {
            if (mostRecentEventTime == null || eventTime.after(mostRecentEventTime)) {
               mostRecentEventTime = eventTime;
            }
         }
      }

      return mostRecentEventTime;
   }

   public int k() {
      return this.i;
   }

   public void c(int severityLevel) {
      this.i = severityLevel;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.b == null ? 0 : this.b.hashCode());
      result = 31 * result + (this.e ? 1231 : 1237);
      result = 31 * result + (this.d == null ? 0 : this.d.hashCode());
      return 31 * result + (this.a == null ? 0 : this.a.hashCode());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         Recommendation other = (Recommendation)obj;
         if (this.b == null) {
            if (other.b != null) {
               return false;
            }
         } else if (!this.b.equals(other.b)) {
            return false;
         }

         if (this.e != other.e) {
            return false;
         } else {
            if (this.d == null) {
               if (other.d != null) {
                  return false;
               }
            } else if (!this.d.equals(other.d)) {
               return false;
            }

            if (this.a == null) {
               if (other.a != null) {
                  return false;
               }
            } else if (!this.a.equals(other.a)) {
               return false;
            }

            return true;
         }
      }
   }

   public class a {
      int a;
      String b;
      long c;

      public a(int optionId, String description) {
         this(optionId, description, 0L);
      }

      public a(int optionId, String description, long value) {
         this.a = optionId;
         this.b = description;
         this.c = value;
      }

      public int a() {
         return this.a;
      }

      public void a(int optionId) {
         this.a = optionId;
      }

      public String b() {
         return this.b;
      }

      public void a(String description) {
         this.b = description;
      }

      public long c() {
         return this.c;
      }

      public void a(long value) {
         this.c = value;
      }

      @Override
      public int hashCode() {
         int prime = 31;
         int result = 1;
         result = 31 * result + (this.b == null ? 0 : this.b.hashCode());
         result = 31 * result + this.a;
         return 31 * result + (int)(this.c ^ this.c >>> 32);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            Recommendation.a other = (Recommendation.a)obj;
            if (this.b == null) {
               if (other.b != null) {
                  return false;
               }
            } else if (!this.b.equals(other.b)) {
               return false;
            }

            return this.a != other.a ? false : this.c == other.c;
         }
      }
   }
}
