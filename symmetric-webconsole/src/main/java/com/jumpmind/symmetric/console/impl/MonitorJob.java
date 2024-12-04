package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.service.IMonitorService;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.job.AbstractJob;
import org.jumpmind.symmetric.job.JobDefaults;
import org.jumpmind.util.RandomTimeSlot;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class MonitorJob extends AbstractJob {
//   private static final String a = "MonitorJob";

   public MonitorJob(ISymmetricEngine engine) {
      this.engine = engine;
      this.jobName = "Monitor";
      this.parameterService = engine.getParameterService();
      this.randomTimeSlot = new RandomTimeSlot(this.parameterService.getExternalId(), this.parameterService.getInt("job.random.max.start.time.ms"));
      this.log = LoggerFactory.getLogger(this.getClass());
   }

   public MonitorJob(ISymmetricEngine engine, ThreadPoolTaskScheduler taskScheduler) {
      super("Monitor", engine, taskScheduler);
      this.log = LoggerFactory.getLogger(this.getClass());
   }

   public JobDefaults getDefaults() {
      return new JobDefaults().schedule("10000").description("Run monitors and generate notifications");
   }

   public void doJob(boolean force) throws Exception {
      if (this.engine != null) {
         ((IMonitorService)this.engine.getExtensionService().getExtensionPoint(IMonitorService.class)).update();
      }
   }
}
