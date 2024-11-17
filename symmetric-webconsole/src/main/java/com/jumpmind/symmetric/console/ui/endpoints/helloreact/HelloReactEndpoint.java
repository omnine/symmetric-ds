package com.jumpmind.symmetric.console.ui.endpoints.helloreact;

import com.jumpmind.symmetric.console.ui.data.VNNode;
import com.jumpmind.symmetric.console.ui.data.HealthInfo;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

import org.jumpmind.symmetric.AbstractSymmetricEngine;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ClientSymmetricEngine;
import org.jumpmind.symmetric.model.*;
import org.jumpmind.symmetric.model.AbstractBatch.Status;

import org.jumpmind.symmetric.route.IDataRouter;
import org.jumpmind.symmetric.service.IIncomingBatchService;
import org.jumpmind.symmetric.service.INodeService;
import com.vaadin.flow.server.VaadinServlet;

import com.jumpmind.symmetric.console.model.NodeStatus;


import org.jumpmind.symmetric.service.IOutgoingBatchService;
import org.jumpmind.symmetric.web.ServletUtils;
import org.jumpmind.symmetric.web.SymmetricEngineHolder;
import org.jumpmind.symmetric.web.FailedEngineInfo;

// The only difference is that @BrowserCallable doesn't support the value attribute.
@Endpoint
@AnonymousAllowed
public class HelloReactEndpoint {
    private long unroutedDataCount = 0;
    @Nonnull
    public String sayHello() {
        VaadinServlet vs = VaadinServlet.getCurrent();
        SymmetricEngineHolder seh = ServletUtils.getSymmetricEngineHolder(vs.getServletContext());
        Map<String, FailedEngineInfo> failedEngineMap = new HashMap<>(seh.getEnginesFailed());
        if(failedEngineMap.isEmpty()) {
            return "Hello";
        }

        int successfulEnginesCount = seh.getEngineCount() - failedEngineMap.size();

        Set<String> failedEngineNames = new HashSet<>(failedEngineMap.keySet());

        String errorMessage = "";
        for (String key : failedEngineMap.keySet()) {
            errorMessage += ((FailedEngineInfo)failedEngineMap.get(key)).getErrorMessage();
        } 

        return errorMessage;
        /*
         * For checking Monitors, need to implement interface IMonitorService extends IBuiltInExtensionPoint
         * 
         */

    }


    @Nonnull
    public HealthInfo checkHealth() {
        Set<String> sysChannels = new HashSet<>(Arrays.asList("heartbeat", "config", "monitor", "dynamic"));
        List<ISymmetricEngine> list = new ArrayList<>(AbstractSymmetricEngine.findEngines());
        ISymmetricEngine engine = list.get(0);        
        HealthInfo healthInfo = new HealthInfo();

        List<Node> offlineNodesList = engine.getNodeService()
        .findOfflineNodes((long)engine.getParameterService().getInt("console.report.as.offline.minutes", 1440));
        if (offlineNodesList.size() == 1
        && offlineNodesList.get(0).getNodeId().equals(engine.getNodeService().getCachedIdentity().getNodeId())) {
            offlineNodesList.clear();
        }

        healthInfo.totalOfflineNodes = offlineNodesList.size();
        // if offlineNodesList.size() == 0, then all nodes are online; All Nodes Online


        // 
        List<IncomingBatch> incomingErrors = engine.getIncomingBatchService().findIncomingBatchErrors(-1);
        List<IncomingBatch> systemIncomingErrors = new ArrayList<>();
  
        for (IncomingBatch batch : incomingErrors) {
           if (sysChannels.contains(batch.getChannelId())) {
              systemIncomingErrors.add(batch);
           }
        }
  
        incomingErrors.removeAll(systemIncomingErrors);
        healthInfo.totalIncomingErrors = incomingErrors.size(); // to check Incoming Batches OK


        OutgoingBatches outgoingErrors = engine.getOutgoingBatchService().getOutgoingBatchErrors(-1);
        outgoingErrors.filterBatchesForChannels(sysChannels);
        healthInfo.totalOutgoingErrors = outgoingErrors.countBatches(true);

        this.unroutedDataCount = healthInfo.unroutedDataCount = engine.getRouterService().getUnroutedDataCount();

        return healthInfo;
    }
   

    @Nonnull
    public ArrayList<NodeStatus> listNodes() {

        // ISymmetricEngine engine = AbstractSymmetricEngine.findEngineByName(engineName);
        List<ISymmetricEngine> list = new ArrayList<>(AbstractSymmetricEngine.findEngines());
        /*
         * File sourceProperties = new File("master-nano190013.properties"); ISymmetricEngine engine = new ClientSymmetricEngine(sourceProperties, false); //
         * no need to register! Node targetNode = engine.getNodeService().findIdentity(); List<Node> nodes = engine.getNodeService().findAllNodes(); add(new
         * Paragraph(greetService.greet(textField.getValue())));
         *
         */
        ISymmetricEngine engine = list.get(0);
//        add(new Paragraph(greetService.greet(engine.getEngineName())));
//        add(new Paragraph("Nodes:"));

        IOutgoingBatchService outgoingService = engine.getOutgoingBatchService();
        IIncomingBatchService incomingService = engine.getIncomingBatchService();
        /*
        List<String> channels = com.jumpmind.symmetric.console.ui.common.am.getConsoleDisplayChannelIds(
                engine, com.jumpmind.symmetric.console.ui.common.am.a.BOTH
        );
*/
        List<String> channels = List.of();

        List<OutgoingBatchSummary> outgoingSummary = outgoingService.findOutgoingBatchSummaryByChannel(
                new Status[]{Status.RQ, Status.NE, Status.ER, Status.LD, Status.QY, Status.RS, Status.RT, Status.SE}
        );
        List<IncomingBatchSummary> incomingSummary = incomingService.findIncomingBatchSummaryByChannel(new Status[]{Status.ER, Status.LD, Status.RS});

        Map<String, NodeStatus> mapNode2Status = new HashMap<>();
//        this.k = new LinkedHashMap<>();
        Set<String> setNodeKeys = new HashSet<String>();
        //f.clear();
        setNodeKeys.addAll(getNodesKeySet(engine));
  
        for (String nodeKey : setNodeKeys) {
            mapNode2Status.put(nodeKey, new NodeStatus(nodeKey));
        }
  
        for (String nodeKey : new HashSet<>(mapNode2Status.keySet())) {
           if (!setNodeKeys.contains(nodeKey)) {
               mapNode2Status.remove(nodeKey);
           } else if (engine.getParameterService().is("heartbeat.update.node.with.batch.status")) {
               mapNode2Status.get(nodeKey).setBatchesInErrorWithAnyNode(engine.getNodeService().findNode(nodeKey).getBatchInErrorCount() > 0);
           }
        }
  
        for (OutgoingBatchSummary bs : outgoingSummary) {
           if (channels.contains(bs.getChannel()) && mapNode2Status.containsKey(bs.getNodeId())) {
              NodeStatus ns = mapNode2Status.get(bs.getNodeId());
              ns.addOutgoingSummary(bs);
              if (ns.getLastOutgoingTime() == null || ns.getLastOutgoingTime().before(bs.getLastBatchUpdateTime())) {
                 ns.setLastOutgoingTime(bs.getLastBatchUpdateTime());
              }
  
              Channel curChannel = engine.getConfigurationService().getChannel(bs.getChannel());
               useChannelMinMax(curChannel, ns);
              if (Status.OK != bs.getStatus() && Status.ER != bs.getStatus()) {
                 ns.incrementOutgoingDataCountRemaining(bs.getDataCount());
                 ns.incrementOutgoingBatchCountRemaining(bs.getBatchCount());
              }
  
              if (bs.isErrorFlag()) {
                 ns.incrementOutgoingDataCountRemaining(bs.getDataCount());
                 ns.setOutgoingErrorFlag(true);
              }
           }
        }
  
        for (IncomingBatchSummary bsx : incomingSummary) {
           if (channels.contains(bsx.getChannel())) {
              if (mapNode2Status.get(bsx.getNodeId()) == null) {
                  mapNode2Status.put(bsx.getNodeId(), new NodeStatus(bsx.getNodeId()));
              }
  
              NodeStatus nsx = mapNode2Status.get(bsx.getNodeId());
              nsx.addIncomingSummary(bsx);
              if (nsx.getLastIncomingTime() == null || nsx.getLastIncomingTime().before(bsx.getLastBatchUpdateTime())) {
                 nsx.setLastIncomingTime(bsx.getLastBatchUpdateTime());
              }
  
              if (bsx.isErrorFlag()) {
                 nsx.setIncomingErrorFlag(true);
                 nsx.addErrorMessage("Incoming Batch ID " + bsx.getMinBatchId());
              }
           }
        }

        //int errorNodeCount = getStatus() == 1
        // int processingNodeCount, getStatus() == 2 or 3




        ArrayList<VNNode> vnNodes = new ArrayList<>();
        for (Node node : engine.getNodeService().findAllNodes()) {
            VNNode vnNode = new VNNode();
            vnNode.setName(node.getNodeId());
            vnNode.setSyncUrl(node.getSyncUrl());
            vnNodes.add(vnNode);

        }

        // get the channel ids
        /*
        List<String> channelIds = new ArrayList<>();
        Map<String, Channel> channels = engine.getConfigurationService().getChannels(false);
        for (Channel channel : channels.values()) {
          if (channel.isReloadFlag()) {
            channelIds.add(channel.getChannelId());
          }
        } 
      */
        // 
        List<NodeGroup> groups = engine.getConfigurationService().getNodeGroups();
        //
        List<NodeGroupLink> links = engine.getConfigurationService().getNodeGroupLinks(false);
        
        //
        Map<String, IDataRouter> routerMap = engine.getRouterService().getRouters();
        //
        List<Trigger> triggers = engine.getTriggerRouterService().getTriggersForCurrentNode(false);

        //
        List<Router> routers = engine.getTriggerRouterService().getRouters();
        // return vnNodes;

        return new ArrayList<>(mapNode2Status.values());
    }

    private void useChannelMinMax(Channel curChannel, NodeStatus ns) {
        if (ns.getMinMaxBatchToSend() == 0 || ns.getMinMaxBatchToSend() > curChannel.getMaxBatchToSend() && curChannel.getMaxBatchToSend() > 0) {
            ns.setMinMaxBatchToSend(curChannel.getMaxBatchToSend());
        }

        if (ns.getMinMaxDataToRoute() == 0 || ns.getMinMaxDataToRoute() > curChannel.getMaxDataToRoute() && curChannel.getMaxDataToRoute() > 0) {
            ns.setMinMaxDataToRoute(curChannel.getMaxDataToRoute());
        }
    }    

    protected HashSet<String> getNodesKeySet(ISymmetricEngine engine) {
        HashSet<String> nodeKeys = new HashSet<>();
        nodeKeys.add(engine.getNodeId());
        INodeService nodeService = engine.getNodeService();
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(nodeService.findNodesToPushTo());
        nodes.addAll(nodeService.findNodesToPull());
        nodes.addAll(nodeService.findNodesWhoPushToMe());
        nodes.addAll(nodeService.findNodesWhoPullFromMe());

        for (Node node : nodes) {
            nodeKeys.add(node.getNodeId());
        }

        return nodeKeys;
    }

    private StringBuilder generateSummary() {
        int allNodeCount = 3;   //this.f.size();
        int processingNodeCount =  1; // this.m.size();
        int errorNodeCount = 1; // this.n.size();
        StringBuilder message = new StringBuilder();
        if (processingNodeCount == 0 && errorNodeCount == 0) {
            if (allNodeCount == 1) {
                message.append("All nodes in sync");
            } else {
                message.append("All <span class='successful'>" + allNodeCount + "</span> nodes in sync");
            }
        } else {
            if (errorNodeCount > 0) {
                message.append("<span class='unsuccessful'>").append(errorNodeCount).append("</span> node");
                if (errorNodeCount > 1) {
                    message.append("s");
                }

                message.append(" in error");
            }

            if (processingNodeCount > 0) {
                if (message.length() > 0) {
                    message.append(", ");
                }

                int syncCount = allNodeCount - processingNodeCount;
                if (syncCount > 1) {
                    message.append("<span class='successful'>").append(syncCount - 1).append("</span> nodes in sync, ");
                }

                message.append("<span class='attention'>").append(processingNodeCount - errorNodeCount).append("</span> node");
                if (processingNodeCount > 1) {
                    message.append("s");
                }

                message.append(" syncing");
            }
        }

        if (unroutedDataCount > 0L) {
            message.append(", ").append("<span class='attention'>").append(unroutedDataCount).append("</span> rows have not yet been put in batches");
        } else {
            message.append(", ").append("<span class='successful'>").append("</span> all changes routed into batches");
        }

        return message;
    }

}