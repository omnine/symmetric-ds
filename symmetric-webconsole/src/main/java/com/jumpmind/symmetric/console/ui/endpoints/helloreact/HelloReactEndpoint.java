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
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.symmetric.model.Channel;
import org.jumpmind.symmetric.model.NodeGroup;
import org.jumpmind.symmetric.model.NodeGroupLink;
import org.jumpmind.symmetric.model.Router;
import org.jumpmind.symmetric.model.Trigger;
import org.jumpmind.symmetric.model.IncomingBatch;
import org.jumpmind.symmetric.model.OutgoingBatches;

import org.jumpmind.symmetric.route.IDataRouter;

import com.vaadin.flow.server.VaadinServlet;


import org.jumpmind.symmetric.web.ServletUtils;
import org.jumpmind.symmetric.web.SymmetricEngineHolder;
import org.jumpmind.symmetric.web.FailedEngineInfo;

// The only difference is that @BrowserCallable doesn't support the value attribute.
@Endpoint
@AnonymousAllowed
public class HelloReactEndpoint {
    private int totalOfflineNodes = 0;

    @Nonnull
    public int getOfflineNodes() {
        return totalOfflineNodes;
    }

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
    public HealthInfo HealthCheck() {
        Set<String> sysChannels = new HashSet<>(Arrays.asList("heartbeat", "config", "monitor", "dynamic"));
        List<ISymmetricEngine> list = new ArrayList<>(AbstractSymmetricEngine.findEngines());
        ISymmetricEngine engine = list.get(0);        
        HealthInfo healthInfo = new HealthInfo();
        OutgoingBatches outgoingErrors = engine.getOutgoingBatchService().getOutgoingBatchErrors(-1);
        outgoingErrors.filterBatchesForChannels(sysChannels);
        healthInfo.totalOutgoingErrors = outgoingErrors.countBatches(true);

        return healthInfo;
    }
   

    @Nonnull
    public ArrayList<VNNode> listNodes() {

        // ISymmetricEngine engine = AbstractSymmetricEngine.findEngineByName(engineName);
        List<ISymmetricEngine> list = new ArrayList<>(AbstractSymmetricEngine.findEngines());
        /*
         * File sourceProperies = new File("master-nano190013.properties"); ISymmetricEngine engine = new ClientSymmetricEngine(sourceProperies, false); //
         * no need to register! Node targetNode = engine.getNodeService().findIdentity(); List<Node> nodes = engine.getNodeService().findAllNodes(); add(new
         * Paragraph(greetService.greet(textField.getValue())));
         *
         */
        ISymmetricEngine engine = list.get(0);
//        add(new Paragraph(greetService.greet(engine.getEngineName())));
//        add(new Paragraph("Nodes:"));

        List<Node> offlineNodesList = engine.getNodeService()
        .findOfflineNodes((long)engine.getParameterService().getInt("console.report.as.offline.minutes", 1440));
        if (offlineNodesList.size() == 1
        && offlineNodesList.get(0).getNodeId().equals(engine.getNodeService().getCachedIdentity().getNodeId())) {
            offlineNodesList.clear();
        }

        // if offlineNodesList.size() == 0, then all nodes are online; All Nodes Online


        // 
        List<IncomingBatch> incomingErrors = engine.getIncomingBatchService().findIncomingBatchErrors(-1);
        List<IncomingBatch> systemIncomingErrors = new ArrayList<>();
  
        Set<String> sysChannels = new HashSet<>(Arrays.asList("heartbeat", "config", "monitor", "dynamic"));
        for (IncomingBatch batch : incomingErrors) {
           if (sysChannels.contains(batch.getChannelId())) {
              systemIncomingErrors.add(batch);
           }
        }
  
        incomingErrors.removeAll(systemIncomingErrors);
        // incomingErrors.size(); to check Incoming Batches OK

      // to check Outgoing Batches OK


        ArrayList<VNNode> vnNodes = new ArrayList<>();
        for (Node node : engine.getNodeService().findAllNodes()) {
            VNNode vnNode = new VNNode();
            vnNode.setName(node.getNodeId());
            vnNode.setSyncUrl(node.getSyncUrl());
            vnNodes.add(vnNode);

        }

        // get the channel ids
        List<String> channelIds = new ArrayList<>();
        Map<String, Channel> channels = engine.getConfigurationService().getChannels(false);
        for (Channel channel : channels.values()) {
          if (channel.isReloadFlag()) {
            channelIds.add(channel.getChannelId());
          }
        } 
      
        // 
        List<NodeGroup> groups = engine.getConfigurationService().getNodeGroups();
        //
        List<NodeGroupLink> links = engine.getConfigurationService().getNodeGroupLinks(false);
        
        //
        Map<String, IDataRouter> routerMap = engine.getRouterService().getRouters();
        //
        List<Trigger> triggers = engine.getTriggerRouterService().getTriggersForCurrentNode(false);

        //
        List<Router> routers = engine.getTriggerRouterService().getRouters();;

        return vnNodes;
    }

}