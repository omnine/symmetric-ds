package com.jumpmind.symmetric.console.impl;

import java.lang.management.ThreadInfo;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.util.AppUtils;

public abstract class StackMonitor implements MonitorExtension, ISymmetricEngineAware {
   protected final int a = 3;
   protected final int b = 30;
   protected ISymmetricEngine c;

   @Override
   public boolean a() {
      return true;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.c = engine;
   }

   protected String a(ThreadInfo info) {
      StringBuilder sb = new StringBuilder();
      sb.append("Stack trace for thread ").append(info.getThreadId()).append(":\n");
      sb.append(AppUtils.formatStackTrace(info.getStackTrace()));
      return sb.toString();
   }
}
