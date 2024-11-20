package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.wrapper.jna.CLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CPUMonitor extends StackMonitor implements IBuiltInExtensionPoint {
   protected final Logger d = LoggerFactory.getLogger(new ge(new long[]{-8805100097329059219L, 460257445400826471L, 6752041828116098195L}).toString());
   protected OperatingSystemMXBean e;
   protected RuntimeMXBean f;
   protected List<StackTraceElement> g;
   protected boolean h = true;

   public CPUMonitor() {
      this.e = ManagementFactory.getOperatingSystemMXBean();
      this.f = ManagementFactory.getRuntimeMXBean();
      this.g = new ArrayList<>();
      this.g.add(new StackTraceElement("java.lang.Object", "wait", null, 0));
      this.g.add(new StackTraceElement("sun.misc.Unsafe", "park", null, 0));
      this.g.add(new StackTraceElement("sun.nio.ch.EPollArrayWrapper", "epollWait", null, 0));
      this.g.add(new StackTraceElement("java.lang.Thread", "sleep", null, 0));
      this.g.add(new StackTraceElement("sun.management.ThreadImpl", "getThreadInfo1", null, 0));
      this.g.add(new StackTraceElement("sun.nio.ch.ServerSocketChannelImpl", "accept", null, 0));
      this.g.add(new StackTraceElement("sun.nio.ch.ServerSocketChannelImpl", "accept0", null, 0));
   }

   @Override
   public String b() {
      return "cpu";
   }

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      int cpuUsage = this.c();
      this.d.debug("CPU usage is {}", cpuUsage);
      event.setValue((long)cpuUsage);
      event.setDetails(this.a((long)cpuUsage, 0L, 0L));
      return event;
   }

   public int c() {
      int availableProcessors = this.e.getAvailableProcessors();
      this.d.debug("Found {} available processors", availableProcessors);
      if (this.h) {
         String line = null;

         try {
            if (Platform.isWindows()) {
               int pid = Kernel32.INSTANCE.GetCurrentProcessId();
               line = this.a(
                  3,
                  "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe",
                  "-Command",
                  "Get-WmiObject -Query \\\"Select * from Win32_PerfFormattedData_PerfProc_Process where IDProcess = "
                     + pid
                     + "\\\" | Select-Object -Property PercentProcessorTime"
               );
               if (line != null) {
                  return Math.min(Integer.parseInt(line.replace(" ", "")), 100);
               }
            } else if (Platform.isMac()) {
               int pid = CLibrary.INSTANCE.getpid();
               line = this.a(25, "top", "-l2", "-pid", String.valueOf(pid));
               if (line != null) {
                  String[] fields = line.trim().split("\\s+");
                  if (fields.length > 2) {
                     return Math.min(Math.round(Float.parseFloat(fields[2]) / (float)availableProcessors), 100);
                  }
               }
            } else if (Platform.isLinux()) {
               int pid = CLibrary.INSTANCE.getpid();
               line = this.a(7, "top", "-bn1", "-p", String.valueOf(pid));
               if (line != null) {
                  String[] fields = line.trim().split("\\s+");
                  if (fields.length > 9) {
                     return Math.min(Math.round(Float.parseFloat(fields[8]) / (float)availableProcessors), 100);
                  }
               }
            }

            this.h = false;
         } catch (RuntimeException var15) {
            this.d
               .info(
                  "Cannot parse native command line output because \"{}: {}\".  Output was: \"{}\"",
                  new Object[]{var15.getClass().getName(), var15.getMessage(), line}
               );
            this.h = false;
         }

         if (!this.h) {
            this.d.info("Switching to CPU time based on JMX");
         }
      }

      long prevUpTime = this.f.getUptime();
      long prevProcessCpuTime = this.d();

      try {
         Thread.sleep(500L);
      } catch (Exception var14) {
      }

      long upTime = this.f.getUptime();
      long processCpuTime = this.d();
      long elapsedCpu = processCpuTime - prevProcessCpuTime;
      long elapsedTime = upTime - prevUpTime;
      return Math.min((int)((float)elapsedCpu / ((float)elapsedTime * 1000.0F * (float)availableProcessors)), 100);
   }

   protected long d() {
      long cpuTime = 0L;

      try {
         Method method = this.e.getClass().getMethod("getProcessCpuTime");
         method.setAccessible(true);
         cpuTime = (Long)method.invoke(this.e);
      } catch (Exception var4) {
         this.d.debug("Caught exception", var4);
      }

      return cpuTime;
   }

   protected String a(int lineNumber, String... args) {
      String ret = null;
      List<String> cmd = new ArrayList<>();

      for (String arg : args) {
         cmd.add(arg);
      }

      this.d.debug("Running command: {}", cmd);
      ProcessBuilder pb = new ProcessBuilder(cmd);
      pb.redirectErrorStream(true);
      Process process = null;

      try {
         process = pb.start();
         process.waitFor();
      } catch (Exception var12) {
         this.d.info("Cannot execute native command line {}: {}", cmd, var12.getMessage());
      }

      if (process != null) {
         ArrayList<String> cmdOutput = new ArrayList<>();

         try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = null;

            while ((line = reader.readLine()) != null) {
               cmdOutput.add(line);
            }
         } catch (Exception var14) {
            this.d.info("Cannot parse native command line {}: {}", cmd, var14.getMessage());
         }

         if (cmdOutput != null && cmdOutput.size() > lineNumber) {
            ret = cmdOutput.get(lineNumber);
         }
      }

      return ret;
   }

   protected String a(long value, long threshold, long period) {
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
      StringBuilder text = new StringBuilder("CPU usage is at ");
      text.append(value).append("%").append(System.lineSeparator()).append(System.lineSeparator());
      Map<ThreadInfo, Float> cpuUsageByThreadMap = this.e();
      Iterator<Entry<ThreadInfo, Float>> iterator = cpuUsageByThreadMap.entrySet().iterator();

      for (int i = 0; i < cpuUsageByThreadMap.size() && i < 3; i++) {
         Entry<ThreadInfo, Float> cpuUsageByThreadEntry = iterator.next();
         ThreadInfo entryInfo = cpuUsageByThreadEntry.getKey();
         ThreadInfo info = threadBean.getThreadInfo(entryInfo.getThreadId(), 30);
         if (info != null) {
            DecimalFormat percentFormat = new DecimalFormat();
            percentFormat.setMaximumFractionDigits(2);
            text.append("Top #")
               .append(i + 1)
               .append(" CPU thread ")
               .append(entryInfo.getThreadName())
               .append(" (ID ")
               .append(entryInfo.getThreadId())
               .append(") is using ")
               .append(percentFormat.format(cpuUsageByThreadEntry.getValue()))
               .append("%")
               .append(System.lineSeparator());
            text.append(this.a(info)).append(System.lineSeparator()).append(System.lineSeparator());
         }
      }

      return text.toString();
   }

   protected Map<ThreadInfo, Float> e() {
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
      Map<Long, Long> initialCpuTimeByThreadMap = new HashMap<>();
      long initialUptime = this.f.getUptime();
      ThreadInfo[] threadInfos = threadBean.dumpAllThreads(false, false);

      for (ThreadInfo info : threadInfos) {
         StackTraceElement[] trace = info.getStackTrace();
         boolean ignore = false;
         if (trace != null && trace.length > 0) {
            for (StackTraceElement element : this.g) {
               if (trace[0].getClassName().equals(element.getClassName()) && trace[0].getMethodName().equals(element.getMethodName())) {
                  ignore = true;
                  break;
               }
            }
         }

         if (!ignore) {
            initialCpuTimeByThreadMap.put(info.getThreadId(), threadBean.getThreadCpuTime(info.getThreadId()));
         }
      }

      try {
         Thread.sleep(10000L);
      } catch (InterruptedException var21) {
      }

      long uptime = this.f.getUptime();
      Map<Long, Long> currentCpuTimeByThreadMap = new HashMap<>();
      threadInfos = threadBean.dumpAllThreads(false, false);

      for (ThreadInfo info : threadInfos) {
         currentCpuTimeByThreadMap.put(info.getThreadId(), threadBean.getThreadCpuTime(info.getThreadId()));
      }

      int cpuCount = this.e.getAvailableProcessors();
      long elapsedTime = uptime - initialUptime;
      Map<ThreadInfo, Float> cpuUsageByThreadMap = new HashMap<>();

      for (ThreadInfo info : threadInfos) {
         Long initialCpuTime = initialCpuTimeByThreadMap.get(info.getThreadId());
         if (initialCpuTime != null) {
            long elapsedCpuTime = currentCpuTimeByThreadMap.get(info.getThreadId()) - initialCpuTime;
            float cpuUsage = (float)elapsedCpuTime / ((float)elapsedTime * 10000.0F * (float)cpuCount);
            cpuUsageByThreadMap.put(info, cpuUsage);
         }
      }

      return cpuUsageByThreadMap.entrySet()
         .stream()
         .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
         .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (val0, val1) -> val0, LinkedHashMap::new));
   }

   @Override
   public boolean a() {
      return false;
   }
}
