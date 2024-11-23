package com.jumpmind.symmetric.console.impl;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ext.IHeartbeatListener;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.symmetric.model.NodeHost;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.util.AppUtils;

public class HeartbeatListener implements IBuiltInExtensionPoint, IHeartbeatListener, ISymmetricEngineAware {
   private ISymmetricEngine a;

   public void heartbeat(Node me) {
      M.b(this.a.getParameterService());
      if (this.a.getParameterService().isRegistrationServer()) {
         INodeService nodeService = this.a.getNodeService();
         String hostName = StringUtils.left(AppUtils.getHostName(), 60);
         String clusterInstanceId = this.a.getClusterService().getInstanceId();
         boolean isClustered = this.a.getClusterService().isClusteringEnabled();
         NodeHost nodeHost = null;

         for (NodeHost host : nodeService.findNodeHosts(me.getNodeId())) {
            if (host.getHostName().equals(hostName) && (!isClustered || clusterInstanceId.equals(host.getInstanceId()))) {
               nodeHost = host;
               break;
            }
         }

         if (nodeHost != null) {
            String oldMac = StringUtils.right(nodeHost.getInstanceId(), 12);
            String newMac = this.a(oldMac);
            if (oldMac == null || newMac != null && newMac.length() == 12 && !newMac.equals(oldMac)) {
               String instanceId = nodeHost.getInstanceId();
               if (StringUtils.isBlank(instanceId)) {
                  instanceId = StringUtils.left(hostName, 23) + "-" + UUID.randomUUID().toString();
               }

               instanceId = instanceId.substring(0, instanceId.length() - 12) + newMac;
               nodeHost.setInstanceId(instanceId);
               nodeService.updateNodeHost(nodeHost);
            }
         }
      }
   }

   public long getTimeBetweenHeartbeatsInSeconds() {
      return 1L;
   }

   private String a(String preferredMacAddress) {
      String macAddress = null;
      if (preferredMacAddress != null) {
         try {
            Enumeration<NetworkInterface> ifcs = NetworkInterface.getNetworkInterfaces();

            while (ifcs.hasMoreElements()) {
               NetworkInterface ifc = ifcs.nextElement();
               if (ifc.getHardwareAddress() != null && ifc.getHardwareAddress().length == 6) {
                  String currentMacAddress = new String(Hex.encodeHex(ifc.getHardwareAddress()));
                  if (preferredMacAddress.equals(currentMacAddress)) {
                     macAddress = currentMacAddress;
                     break;
                  }
               }
            }
         } catch (SocketException var7) {
         }
      }

      if (macAddress == null) {
         try {
            Enumeration<NetworkInterface> ifcs = NetworkInterface.getNetworkInterfaces();

            while (ifcs.hasMoreElements()) {
               NetworkInterface ifc = ifcs.nextElement();
               String name = StringUtils.trimToEmpty(ifc.getDisplayName()).toUpperCase();
               if (!ifc.isLoopback()
                       && !ifc.isVirtual()
                       && !ifc.isPointToPoint()
                       && ifc.getHardwareAddress() != null
                       && ifc.getHardwareAddress().length == 6
                       && !name.contains("DOCKER")
                       && !name.contains("VMWARE")
                       && !name.contains("VIRTUAL")) {
                  macAddress = new String(Hex.encodeHex(ifc.getHardwareAddress()));
                  break;
               }
            }
         } catch (SocketException var6) {
         }
      }

      return macAddress;
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.a = engine;
   }
}
