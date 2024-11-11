package com.jumpmind.symmetric.console.ui.endpoints.helloreact;

import com.jumpmind.symmetric.console.ui.data.VNNode;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;

import java.util.List;
import java.util.ArrayList;

import org.jumpmind.symmetric.AbstractSymmetricEngine;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.ClientSymmetricEngine;
import org.jumpmind.symmetric.model.Node;

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

        return vnNodes;
    }

}