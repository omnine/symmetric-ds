package com.jumpmind.symmetric.console.service.impl;

import com.jumpmind.symmetric.console.impl.G;
import com.jumpmind.symmetric.console.service.IBackgroundNoHangupService;
import com.vaadin.flow.component.UI;
import java.util.concurrent.TimeUnit;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class BackgroundNoHangupService implements IBackgroundNoHangupService, IBuiltInExtensionPoint, ISymmetricEngineAware {
   protected final Logger log = LoggerFactory.getLogger(this.getClass());
   protected ISymmetricEngine engine;
   private transient ThreadPoolTaskScheduler taskScheduler;

   @Override
   public void queueWork(final G<? extends Object> refreshing, final com.jumpmind.symmetric.console.ui.common.G controller) {
      this.taskScheduler.getScheduledExecutor().schedule(new Runnable() {
         @Override
         public void run() {
            BackgroundNoHangupService.this.refresh(refreshing, controller);
         }
      }, 0L, TimeUnit.MILLISECONDS);
   }

   protected void refresh(G<Object> refreshing, com.jumpmind.symmetric.console.ui.common.G controller) {
      try {
         String engineName = "not available";
         if (this.engine != null) {
            engineName = this.engine.getEngineName();
         }

         MDC.put("engineName", engineName);
         long ts = System.currentTimeMillis();
         Object data = refreshing.onBackgroundDataRefresh(this.engine);
         long timeItTookInMs = System.currentTimeMillis() - ts;
         if (timeItTookInMs > 10000L) {
            this.log.info("refreshing data for {} for engine: {} took {}ms", new Object[]{refreshing.getClass().getSimpleName(), engineName, timeItTookInMs});
         }

         if (controller != null) {
            UI ui = controller.getUI().orElse(null);
            if (ui != null && ui.getElement().getNode().isAttached()) {
               ui.access(() -> {
                  try {
                     long currentTime = System.currentTimeMillis();
                     refreshing.onBackgroundUIRefresh(data);
                     long timeItTookInMillis = System.currentTimeMillis() - currentTime;
                     if (timeItTookInMillis > 10000L) {
                        this.log.info("refreshing ui for {} took {}ms", refreshing.getClass().getSimpleName(), timeItTookInMillis);
                     }
                  } catch (Throwable var7x) {
                     refreshing.onUIError(var7x);
                     this.log.warn("Error during background refresh of " + refreshing.getClass(), var7x);
                  }
               });
            }
         }
      } catch (Throwable var10) {
         this.log.error("Exception while refreshing " + refreshing.getClass(), var10);
      }
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.engine = engine;
      this.taskScheduler = new ThreadPoolTaskScheduler();
      this.taskScheduler.setThreadNamePrefix("background-nohup-");
      this.taskScheduler.setPoolSize(3);
      this.taskScheduler.initialize();
   }
}
