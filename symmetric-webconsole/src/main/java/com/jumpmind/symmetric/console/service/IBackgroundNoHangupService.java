package com.jumpmind.symmetric.console.service;

import com.jumpmind.symmetric.console.impl.G;
import org.jumpmind.extension.IBuiltInExtensionPoint;

public interface IBackgroundNoHangupService extends IBuiltInExtensionPoint {
   void queueWork(G<? extends Object> var1, com.jumpmind.symmetric.console.ui.common.G var2);
}
