package com.jumpmind.symmetric.console.ui.endpoints.helloreact;

import com.jumpmind.symmetric.console.ui.data.VNNode;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;

import java.util.List;
import java.util.ArrayList;
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

import org.jumpmind.symmetric.route.IDataRouter;

import com.vaadin.flow.server.VaadinServlet;


import org.jumpmind.symmetric.web.ServletUtils;
import org.jumpmind.symmetric.web.SymmetricEngineHolder;
import org.jumpmind.symmetric.web.FailedEngineInfo;

@Endpoint
@AnonymousAllowed
public class HelloReactEndpoint {
    @Nonnull
    public String sayHello(@Nonnull String name) {
        if (name.isEmpty()) {
            return "Hello stranger";
        } else {
            return "Hello " + name;
        }
    }


    public void checkEngineHealthy() {
        VaadinServlet vs = VaadinServlet.getCurrent();
        SymmetricEngineHolder seh = ServletUtils.getSymmetricEngineHolder(vs.getServletContext());
        Map<String, FailedEngineInfo> failedEngineMap = new HashMap<>(seh.getEnginesFailed());
        Set<String> failedEngineNames = new HashSet<>(failedEngineMap.keySet());
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