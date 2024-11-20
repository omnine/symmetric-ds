package com.jumpmind.symmetric.console.service;

import com.jumpmind.symmetric.console.impl.IRefresh;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public interface IBackgroundNoHangupService extends IBuiltInExtensionPoint {
   void queueWork(IRefresh<? extends Object> var1, com.jumpmind.symmetric.console.ui.common.IConsoleController var2);
}
