package com.jumpmind.symmetric.console.impl;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IMonitorService;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.statistic.JobStats;

public class JobTrendingMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      List<JobStats> jobStatsList = this.a.getStatisticService().getJobStatsForNode(this.a.getNodeId());
      jobStatsList.sort((s0, s1) -> s1.getStartTime().compareTo(s0.getStartTime()));
      if (!jobStatsList.isEmpty()) {
         Map<String, List<Long>> runtimesByJobMap = new HashMap<>();
         Map<String, List<Long>> ratesByJobMap = new HashMap<>();

         for (JobStats stats : jobStatsList) {
            String jobName = stats.getJobName();
            List<Long> runtimeList = runtimesByJobMap.get(jobName);
            if (runtimeList == null) {
               runtimeList = new ArrayList<>();
            }

            long runtime = stats.getEndTime().getTime() - stats.getStartTime().getTime();
            runtimeList.add(runtime);
            runtimesByJobMap.put(jobName, runtimeList);
            List<Long> rateList = ratesByJobMap.get(jobName);
            if (rateList == null) {
               rateList = new ArrayList<>();
            }

            rateList.add(runtime >= 60000L ? stats.getProcessedCount() / (runtime / 60000L) : -1L);
            ratesByJobMap.put(jobName, rateList);
         }

         IMonitorService monitorService = (IMonitorService)this.a.getExtensionService().getExtensionPoint(IMonitorService.class);
         Date now = new Date();
         List<fn.a> unresolvedJobDetailsList = null;
         Gson gson = com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson();

         for (MonitorEvent existingEvent : monitorService.getMonitorEventsByMonitorId(monitor.getMonitorId())) {
            Date notBefore = existingEvent.getNotBefore();
            if (!existingEvent.isResolved() && (notBefore == null || !notBefore.after(now)) && existingEvent.getNodeId().equals(this.a.getNodeId())) {
               fT recommendation = (fT)gson.fromJson(existingEvent.getDetails(), fT.class);
               List<LinkedTreeMap<String, String>> linkedTreeMapList = (List<LinkedTreeMap<String, String>>)recommendation.c("jobDetailsList");
               Type jobDetailsListType = (new TypeToken<List<fn.a>>() {
               }).getType();
               unresolvedJobDetailsList = (List<fn.a>)gson.fromJson(gson.toJson(linkedTreeMapList, jobDetailsListType), jobDetailsListType);
               break;
            }
         }

         long largestPeakRuntimePercentIncrease = 0L;
         List<fn.a> jobDetailsList = new ArrayList<>();
         Iterator var43 = runtimesByJobMap.entrySet().iterator();

         while (true) {
            List<Long> runtimeListx;
            int runtimeCount;
            long latestRuntime;
            String jobNamex;
            while (true) {
               if (!var43.hasNext()) {
                  event.setValue(largestPeakRuntimePercentIncrease);
                  String problemDescription = "";

                  for (fn.a details : jobDetailsList) {
                     String var45 = problemDescription
                        + "The "
                        + details.a()
                        + " job took "
                        + details.e()
                        + "% longer than the previous peak runtime and "
                        + details.f()
                        + "% longer than the average runtime.";
                     Long averageRatePercentIncrease = details.g();
                     if (averageRatePercentIncrease == null) {
                        problemDescription = var45 + "\n";
                     } else if (averageRatePercentIncrease > 0L) {
                        problemDescription = var45 + " Its processing rate is " + averageRatePercentIncrease + "% slower than average.\n";
                     } else if (averageRatePercentIncrease < 0L) {
                        problemDescription = var45 + " Its processing rate is " + Math.abs(averageRatePercentIncrease) + "% faster than average.\n";
                     } else {
                        problemDescription = var45 + " Its processing rate is the same as the average.\n";
                     }
                  }

                  if (problemDescription != null) {
                     fT recommendation = new fT(problemDescription.trim(), null, false);
                     recommendation.a("jobDetailsList", jobDetailsList);
                     event.setDetails(com.jumpmind.symmetric.console.ui.common.am.getMonitorEventGson().toJson(recommendation));
                  }

                  return event;
               }

               Entry<String, List<Long>> runtimesByJobEntry = (Entry<String, List<Long>>)var43.next();
               jobNamex = runtimesByJobEntry.getKey();
               runtimeListx = runtimesByJobEntry.getValue();
               runtimeCount = runtimeListx.size();
               if (runtimeCount >= 100 || runtimeCount >= 10 && !jobNamex.equals("Routing") && !jobNamex.equals("Push") && !jobNamex.equals("Pull")) {
                  latestRuntime = runtimeListx.get(0);
                  if (unresolvedJobDetailsList == null) {
                     break;
                  }

                  fn.a detailsToAdd = null;

                  for (fn.a detailsx : unresolvedJobDetailsList) {
                     if (detailsx.a().equals(jobNamex)) {
                        long peakRuntime = detailsx.b();
                        long peakRuntimePercentIncrease = (latestRuntime - peakRuntime) * 100L / peakRuntime;
                        if (peakRuntimePercentIncrease > monitor.getThreshold()) {
                           detailsToAdd = detailsx;
                           detailsx.a(peakRuntimePercentIncrease);
                           Long averageRuntime = detailsx.c();
                           detailsx.b((latestRuntime - averageRuntime) * 100L / averageRuntime);
                           Long averageRate = detailsx.d();
                           if (averageRate > 0L) {
                              List<Long> rateList = ratesByJobMap.get(jobNamex);
                              detailsx.c((rateList.get(0) - averageRate) * 100L / averageRate);
                           }

                           jobDetailsList.add(detailsx);
                           if (peakRuntimePercentIncrease > largestPeakRuntimePercentIncrease) {
                              largestPeakRuntimePercentIncrease = peakRuntimePercentIncrease;
                           }
                        }
                        break;
                     }
                  }

                  if (detailsToAdd == null) {
                     break;
                  }
               }
            }

            long peakRuntime = 0L;
            long averageRuntimex = 0L;

            for (int i = 1; i < runtimeCount; i++) {
               long runtime = runtimeListx.get(i);
               if (runtime > peakRuntime) {
                  peakRuntime = runtime;
               }

               averageRuntimex += runtime;
            }

            if (peakRuntime != 0L) {
               long peakRuntimePercentIncrease = (latestRuntime - peakRuntime) * 100L / peakRuntime;
               if (peakRuntimePercentIncrease > monitor.getThreshold()) {
                  averageRuntimex /= (long)(runtimeListx.size() - 1);
                  long averageRuntimePercentIncrease = (latestRuntime - averageRuntimex) * 100L / averageRuntimex;
                  List<Long> rateList = ratesByJobMap.get(jobNamex);
                  int validRateCount = 0;
                  long averageRatex = 0L;

                  for (int i = 1; i < rateList.size(); i++) {
                     long rate = rateList.get(i);
                     if (rate >= 0L) {
                        averageRatex += rate;
                        validRateCount++;
                     }
                  }

                  Long averageRatePercentIncrease = null;
                  if (validRateCount > 0 && averageRatex / (long)validRateCount > 0L) {
                     averageRatex /= (long)validRateCount;
                     averageRatePercentIncrease = (rateList.get(0) - averageRatex) * 100L / averageRatex;
                  }

                  jobDetailsList.add(
                     new fn.a(
                        jobNamex,
                        peakRuntime,
                        averageRuntimex,
                        averageRatex,
                        peakRuntimePercentIncrease,
                        averageRuntimePercentIncrease,
                        averageRatePercentIncrease
                     )
                  );
               }

               if (peakRuntimePercentIncrease > largestPeakRuntimePercentIncrease) {
                  largestPeakRuntimePercentIncrease = peakRuntimePercentIncrease;
               }
            }
         }
      } else {
         return event;
      }
   }

   @Override
   public boolean a(MonitorEvent event, fT recommendation) {
      return true;
   }

   @Override
   public String b() {
      return "jobTrending";
   }

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }

   class a {
      private String b;
      private Long c;
      private Long d;
      private Long e;
      private Long f;
      private Long g;
      private Long h;

      public a(
         String jobName,
         Long peakRuntime,
         Long averageRuntime,
         Long averageRate,
         Long peakRuntimePercentIncrease,
         Long averageRuntimePercentIncrease,
         Long averageRatePercentIncrease
      ) {
         this.b = jobName;
         this.c = peakRuntime;
         this.d = averageRuntime;
         this.e = averageRate;
         this.f = peakRuntimePercentIncrease;
         this.g = averageRuntimePercentIncrease;
         this.h = averageRatePercentIncrease;
      }

      public String a() {
         return this.b;
      }

      public Long b() {
         return this.c;
      }

      public Long c() {
         return this.d;
      }

      public Long d() {
         return this.e;
      }

      public Long e() {
         return this.f;
      }

      public void a(Long peakRuntimePercentIncrease) {
         this.f = peakRuntimePercentIncrease;
      }

      public Long f() {
         return this.g;
      }

      public void b(Long averageRuntimePercentIncrease) {
         this.g = averageRuntimePercentIncrease;
      }

      public Long g() {
         return this.h;
      }

      public void c(Long averageRatePercentIncrease) {
         this.h = averageRatePercentIncrease;
      }
   }
}
