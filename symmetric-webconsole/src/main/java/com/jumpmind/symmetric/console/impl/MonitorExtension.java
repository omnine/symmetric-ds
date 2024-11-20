package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import org.jumpmind.extension.IExtensionPoint;

public interface MonitorExtension extends IExtensionPoint {
   MonitorEvent a(Monitor var1);

   boolean a();

   String b();
}
