package com.jumpmind.symmetric.console.service;

import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.properties.TypedProperties;

public interface IMailService extends IBuiltInExtensionPoint {
   String sendEmail(String var1, String var2, String var3);

   String sendEmail(String var1, String var2, String var3, TypedProperties var4);

   String testTransport(TypedProperties var1);
}
