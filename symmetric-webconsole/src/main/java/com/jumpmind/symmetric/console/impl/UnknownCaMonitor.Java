package com.jumpmind.symmetric.console.impl;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.MonitorEvent;
import com.jumpmind.symmetric.console.service.IConsoleEventService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.security.ISecurityService;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.transport.http.HttpConnection;
import org.jumpmind.symmetric.util.LogSummaryAppenderUtils;
import org.jumpmind.util.FormatUtils;
import org.jumpmind.util.LogSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnknownCaMonitor implements InsightMonitor, IBuiltInExtensionPoint, ISymmetricEngineAware {
   private ISymmetricEngine a;
   private Logger b = LoggerFactory.getLogger(this.getClass());

   @Override
   public MonitorEvent a(Monitor monitor) {
      MonitorEvent event = new MonitorEvent();
      Set<String> nodeIdSet = new HashSet<>();

      for (LogSummary logSummary : LogSummaryAppenderUtils.getLogSummaryErrors(this.a.getEngineName())) {
         String message = logSummary.getMessage();
         if (StringUtils.containsIgnoreCase(message, "Failed to process incoming batch from node '")
            && StringUtils.containsIgnoreCase(logSummary.getStackTrace(), "PKIX path building failed")) {
            String nodeId = message.substring(message.indexOf("'") + 1);
            nodeId = nodeId.substring(0, nodeId.indexOf("'"));
            nodeIdSet.add(nodeId);
         }
      }

      for (LogSummary logSummaryx : LogSummaryAppenderUtils.getLogSummaryWarnings(this.a.getEngineName())) {
         String message = logSummaryx.getMessage();
         if (StringUtils.containsIgnoreCase(message, "Could not communicate with node")
            && StringUtils.containsIgnoreCase(logSummaryx.getStackTrace(), "PKIX path building failed")) {
            String nodeId = message.substring(message.indexOf("'") + 1);
            nodeId = nodeId.substring(0, nodeId.indexOf("'"));
            nodeId = nodeId.substring(nodeId.lastIndexOf(":") + 1);
            nodeIdSet.add(nodeId);
         }
      }

      int nodeIdCount = nodeIdSet.size();
      event.setValue((long)nodeIdCount);
      if (nodeIdCount > 0) {
         String problemDescription;
         String optionDescription;
         if (nodeIdCount == 1) {
            problemDescription = "An unknown certificate authority error occurred while communicating with the '" + nodeIdSet.iterator().next() + "' node.";
            optionDescription = "Import the certificates from the '" + nodeIdSet.iterator().next() + "' node and restart SymmetricDS";
         } else {
            problemDescription = nodeIdCount
               + " unknown certificate authority errors occurred while communicating with the following nodes: "
               + nodeIdSet
               + ".";
            optionDescription = "Import the certificates from the following nodes and restart SymmetricDS: " + nodeIdSet;
         }

         String actionDescription = "Import the missing certificates and restart SymmetricDS.";
         Recommendation recommendation = new Recommendation(problemDescription, actionDescription, true);
         List<Recommendation.a> options = new ArrayList<>();
         options.add(recommendation.new a(1, optionDescription));
         recommendation.a(options);
         recommendation.a("nodeIdSet", nodeIdSet);
         event.setDetails(com.jumpmind.symmetric.console.ui.common.Helper.getMonitorEventGson().toJson(recommendation));
      }

      return event;
   }

   @Override
   public boolean a(MonitorEvent event, Recommendation recommendation) {
      Collection<String> nodeIdSet = (Collection<String>)recommendation.c("nodeIdSet");
      if (nodeIdSet == null) {
         this.b.error("Failed to approve Unknown Certificate Authority insight because node ID set was null.");
         return false;
      } else {
         INodeService nodeService = this.a.getNodeService();

         for (String nodeId : nodeIdSet) {
            Node node = nodeService.findNode(nodeId);
            if (node == null) {
               this.b.error("Failed to approve Unknown Certificate Authority insight because the node with the following ID could not be found: " + nodeId);
               return false;
            }

            URL syncUrl;
            try {
               syncUrl = new URL(node.getSyncUrl());
            } catch (MalformedURLException var26) {
               this.b.error("Failed to approve Unknown Certificate Authority insight because sync URL was invalid: " + node.getSyncUrl(), var26);
               return false;
            }

            if (!"https".equals(syncUrl.getProtocol())) {
               this.b.error("Failed to approve Unknown Certificate Authority insight because sync URL does not use HTTPS protocol: " + node.getSyncUrl());
               return false;
            }

            HttpConnection connection;
            try {
               connection = new HttpConnection(syncUrl);
            } catch (IOException var25) {
               this.b.error("Failed to approve Unknown Certificate Authority insight because failed to connect to sync URL: " + node.getSyncUrl(), var25);
               return false;
            }

            connection.setHostnameVerifier((string, session) -> true);

            SSLContext sslContext;
            try {
               sslContext = SSLContext.getInstance("TLS");
               X509TrustManager trustManager = new X509TrustManager() {
                  private X509Certificate[] b = new X509Certificate[0];

                  @Override
                  public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                  }

                  @Override
                  public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                     this.b = xcs;
                  }

                  @Override
                  public X509Certificate[] getAcceptedIssuers() {
                     return this.b;
                  }
               };
               sslContext.init(null, new TrustManager[]{trustManager}, null);
            } catch (Exception var24) {
               this.b.error("Failed to approve Unknown Certificate Authority insight because failed to initialize SSL context", var24);
               connection.close();
               return false;
            }

            connection.setSslSocketFactory(sslContext.getSocketFactory());
            ISecurityService securityService = this.a.getSecurityService();
            IConsoleEventService consoleEventService = (IConsoleEventService)this.a.getExtensionService().getExtensionPoint(IConsoleEventService.class);

            for (Certificate cert : connection.getServerCertificates()) {
               if (cert instanceof X509Certificate) {
                  try {
                     String certString = FormatUtils.convertToPem((X509Certificate)cert);
                     TrustedCertificateEntry entry = securityService.createTrustedCert(certString.getBytes(), "pem", null, null);
                     securityService.installTrustedCert(entry);
                     String subject = "";
                     if (entry.getTrustedCertificate() instanceof X509Certificate x509Certificate) {
                        subject = x509Certificate.getSubjectX500Principal().getName().replace(",", ", ");
                     }

                     consoleEventService.addEvent(new ConsoleEvent(event.getApprovedBy(), "Import Authority", nodeId, nodeId, null, subject));
                  } catch (CertificateEncodingException var23) {
                     connection.close();
                  }
               }
            }

            connection.disconnect();
            connection.close();
         }

         System.exit(0);
         return true;
      }
   }

   @Override
   public String b() {
      return "unknownCa";
   }

   @Override
   public boolean a() {
      return false;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
