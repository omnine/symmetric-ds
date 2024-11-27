package com.jumpmind.symmetric.console.ui.endpoints.proapi;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.NodeMonitors;
import com.jumpmind.symmetric.console.model.RecentActivity;
import com.jumpmind.symmetric.console.service.IMonitorService;
import com.jumpmind.symmetric.console.service.impl.MonitorService;
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
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Comparator;

import org.jumpmind.symmetric.AbstractSymmetricEngine;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.model.*;
import org.jumpmind.symmetric.model.AbstractBatch.Status;
import org.jumpmind.symmetric.model.ProcessInfo.ProcessStatus;

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
public class ProAPIEndpoint {
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

        StringBuilder errorMessage = new StringBuilder();
        for (String key : failedEngineMap.keySet()) {
            errorMessage.append(((FailedEngineInfo) failedEngineMap.get(key)).getErrorMessage());
        } 

        return errorMessage.toString();
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

        healthInfo.engineNodeId = engine.getNodeId();

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
/*
        IMonitorService ims = (IMonitorService)engine.getExtensionService().getExtensionPoint(IMonitorService.class);

        List<MonitorEvent> monitorEvents = ims.getMonitorEventsFiltered(-1, null, 0, engine.getNodeId(), false);
        monitorEvents = monitorEvents.stream().filter(event -> !event.isInsight()).collect(Collectors.toList());

        List<Monitor> monitors = this.b
                .getMonitorService()
                .getActiveMonitorsForNode(engine.getParameterService().getString("group.id"), engine.getParameterService().getString("external.id"));
        monitors = monitors.stream().filter(monitor -> !monitor.isInsight()).collect(Collectors.toList());



        healthInfo.totalFailedMonitors = monitorEvents.size();
*/
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
        
        List<String> channels = getConsoleDisplayChannelIds(
                engine, ChannelType.BOTH
        );


        List<OutgoingBatchSummary> outgoingSummary = outgoingService.findOutgoingBatchSummaryByChannel(
                Status.RQ, Status.NE, Status.ER, Status.LD, Status.QY, Status.RS, Status.RT, Status.SE);
        List<IncomingBatchSummary> incomingSummary = incomingService.findIncomingBatchSummaryByChannel(new Status[]{Status.ER, Status.LD, Status.RS});

        Map<String, NodeStatus> mapNode2Status = new HashMap<>();
//        this.k = new LinkedHashMap<>();
        Set<String> setNodeKeys = new HashSet<>();
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

    @Nonnull
    public MultiResult getMonitorEvents() {
        List<ISymmetricEngine> list = new ArrayList<>(AbstractSymmetricEngine.findEngines());
        ISymmetricEngine engine = list.get(0);
        Map<String, NodeMonitors> h = new HashMap<>();
        engine.getNodeService().findAllNodes().forEach(node -> h.put(node.getNodeId(), new NodeMonitors(node.getNodeId())));
        IMonitorService monitorService = engine.getExtensionService().getExtensionPoint(IMonitorService.class);
        ((MonitorService) monitorService).setSymmetricEngine(engine);

        monitorService.getMonitorEvents().forEach(monitorEvent -> {
          if (!monitorEvent.isResolved() && !monitorEvent.isInsight()) {
             if (h.get(monitorEvent.getNodeId()) == null) {
                h.put(monitorEvent.getNodeId(), new NodeMonitors(monitorEvent.getNodeId()));
             }
 
             h.get(monitorEvent.getNodeId()).getMonitorEvents().put(monitorEvent.getType(), monitorEvent);
          }
       });
       ArrayList<Monitor> monitors = (ArrayList<Monitor>) monitorService
       .getMonitors()
       .stream()
       .filter(monitor -> !monitor.isInsight())
       .sorted(Comparator.comparing(Monitor::getLastUpdateTime).reversed())
       .collect(Collectors.toList());

        MultiResult mr = new MultiResult();
        mr.monitors = monitors;
        mr.nodeMonitors = new ArrayList<>(h.values());
        return mr;

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

    public ArrayList<RecentActivity> getRecentActivities() {
        List<ISymmetricEngine> list = new ArrayList<>(AbstractSymmetricEngine.findEngines());
        ISymmetricEngine engine = list.get(0);
        Set<String> setChannels = Set.of();
        /*
        this.i = new HashSet<>(
                com.jumpmind.symmetric.console.ui.common.am.getConsoleDisplayChannelIds(
                        this.b.getSymmetricEngine(), com.jumpmind.symmetric.console.ui.common.am.a.BOTH
                )
        );
*/
        Map<String, RecentActivity> mapRAs = new HashMap<>();

        for (ProcessInfo p : engine.getStatisticManager().getProcessInfosThatHaveDoneWork()) {
            if (setChannels.contains(p.getCurrentChannelId())) {
                ProcessType pt = p.getProcessType();
                RecentActivity recentActivity = null;
                StringBuilder message = new StringBuilder();
                boolean running = !p.getStatus().equals(ProcessStatus.OK);
                String type = null;
                String sourceNodeId = p.getSourceNodeId() == null ? "registration node" : p.getSourceNodeId();
                if (pt.equals(ProcessType.ROUTER_JOB) || pt.equals(ProcessType.ROUTER_READER)) {
                    recentActivity = mapRAs.get(ProcessType.ROUTER_JOB.name());
                    if (p.getTotalDataCount() > 0L) {
                        message.append(running ? "Routing " : "Routed ").append(p.getCurrentDataCount()).append(" rows");
                        type = ProcessType.ROUTER_JOB.name();
                    }
                } else if (pt.equals(ProcessType.PULL_HANDLER_TRANSFER)) {
                    recentActivity = mapRAs.get(ProcessType.PULL_HANDLER_TRANSFER.name() + p.getTargetNodeId());
                    if (p.getTotalDataCount() > 0L) {
                        message.append(running ? "Sending " : "Sent ")
                                .append(p.getTotalDataCount())
                                .append(" rows to ")
                                .append(p.getTargetNodeId())
                                .append(" (pull)");
                        type = ProcessType.PULL_HANDLER_TRANSFER.name() + p.getTargetNodeId();
                    }
                } else if (pt.equals(ProcessType.PULL_JOB_LOAD)) {
                    recentActivity = mapRAs.get(ProcessType.PULL_JOB_LOAD.name() + p.getTargetNodeId());
                    if (p.getTotalDataCount() > 0L) {
                        message.append(running ? "Loading " : "Loaded ").append(p.getCurrentDataCount()).append(" rows from ").append(sourceNodeId).append(" (pull)");
                        type = ProcessType.PULL_JOB_LOAD.name() + p.getTargetNodeId();
                    }
                } else if (pt.equals(ProcessType.PUSH_HANDLER_LOAD)) {
                    recentActivity = mapRAs.get(ProcessType.PUSH_HANDLER_LOAD.name() + p.getSourceNodeId());
                    if (p.getTotalDataCount() > 0L) {
                        message.append(running ? "Loading " : "Loaded ").append(p.getTotalDataCount()).append(" rows from ").append(sourceNodeId).append(" (push)");
                        type = ProcessType.PUSH_HANDLER_LOAD.name() + p.getSourceNodeId();
                    }
                } else if (pt.equals(ProcessType.PULL_JOB_TRANSFER)) {
                    recentActivity = mapRAs.get(ProcessType.PULL_JOB_TRANSFER.name() + p.getSourceNodeId());
                    if (p.getTotalDataCount() > 0L) {
                        message.append(running ? "Receiving " : "Received ")
                                .append(p.getTotalDataCount())
                                .append(" rows from ")
                                .append(sourceNodeId)
                                .append(" (pull)");
                        type = ProcessType.PULL_JOB_TRANSFER.name() + p.getSourceNodeId();
                    }
                } else if (pt.equals(ProcessType.PUSH_JOB_EXTRACT)) {
                    recentActivity = mapRAs.get(ProcessType.PUSH_JOB_EXTRACT.name() + p.getTargetNodeId());
                    if (p.getTotalDataCount() > 0L) {
                        message.append(running ? "Extracting " : "Extracted ")
                                .append(p.getTotalDataCount())
                                .append(" rows for ")
                                .append(p.getTargetNodeId())
                                .append(" (push)");
                        type = ProcessType.PUSH_JOB_EXTRACT.name() + p.getTargetNodeId();
                    }
                } else if (pt.equals(ProcessType.PULL_HANDLER_EXTRACT)) {
                    recentActivity = mapRAs.get(ProcessType.PULL_HANDLER_EXTRACT.name() + p.getTargetNodeId());
                    if (p.getTotalDataCount() > 0L) {
                        message.append(running ? "Extracting " : "Extracted ")
                                .append(p.getTotalDataCount())
                                .append(" rows for ")
                                .append(p.getTargetNodeId())
                                .append(" (pull)");
                        type = ProcessType.PULL_HANDLER_EXTRACT.name() + p.getTargetNodeId();
                    }
                } else if (pt.equals(ProcessType.PUSH_JOB_TRANSFER)) {
                    recentActivity = mapRAs.get(ProcessType.PUSH_JOB_TRANSFER.name() + p.getTargetNodeId());
                    if (p.getTotalDataCount() > 0L) {
                        message.append(running ? "Sending " : "Sent ")
                                .append(p.getCurrentDataCount())
                                .append(" rows to ")
                                .append(p.getTargetNodeId())
                                .append(" (push)");
                        type = ProcessType.PUSH_JOB_TRANSFER.name() + p.getTargetNodeId();
                    }
                } else if (pt.equals(ProcessType.INITIAL_LOAD_EXTRACT_JOB)) {
                    type = ProcessType.INITIAL_LOAD_EXTRACT_JOB.name() + p.getTargetNodeId();
                    recentActivity = mapRAs.get(ProcessType.INITIAL_LOAD_EXTRACT_JOB.name() + p.getTargetNodeId());
                    message.append(running ? "Extracting " : "Extracted ")
                            .append(p.getCurrentDataCount())
                            .append(" reload rows in background for ")
                            .append(p.getTargetNodeId());
                }

                if (recentActivity == null && message.length() > 0) {
                    recentActivity = new RecentActivity(message.toString(), running, p.getTotalDataCount(), p.getStartTime(), p.getEndTime());
                    mapRAs.put(type, recentActivity);
                } else if (message.length() > 0) {
                    recentActivity.setMessage(message.toString());
                    if (p.getTotalDataCount() > 0L) {
                        recentActivity.setStartTime(p.getStartTime());
                    }
                }

                if (recentActivity != null) {
                    recentActivity.setRunning(running);
                    if (running) {
                        recentActivity.setEndTime(null);
                    } else {
                        recentActivity.setEndTime(p.getEndTime());
                    }
                }
            }
        }
        return new ArrayList<>(mapRAs.values());
    }

    //do not define any static method, otherwise Hilla will try to generate all variables inside it into typescript.
    private enum ChannelType {
        OUTGOING,
        INCOMING,
        BOTH
     }

    private List<String> getConsoleDisplayChannelIds(ISymmetricEngine engine, ChannelType type) {
        List<String> channels;
        if (engine.getParameterService().is("console.web.hide.system.info")) {  // this is generally true
           Set<String> channelList = new HashSet<>();
           /* we don't care the file sync
           if (engine.getParameterService().is("file.sync.enable")) {
              for (FileTriggerRouter ftr : engine.getFileSyncService().getFileTriggerRoutersForCurrentNode(false)) {
                 String channel = ftr.getFileTrigger() != null ? ftr.getFileTrigger().getChannelId() : "";
                 String reloadChannel = ftr.getFileTrigger() != null ? ftr.getFileTrigger().getReloadChannelId() : "";
                 if (channel.length() > 0 && !isHidableChannel(channel, engine)) {
                    channelList.add(channel);
                 }
  
                 if (reloadChannel.length() > 0 && !isHidableChannel(reloadChannel, engine)) {
                    channelList.add(reloadChannel);
                 }
              }
           }
           */
  
           for (TriggerRouter tr : engine.getTriggerRouterService().getTriggerRouters(false)) {
              String trSourceGroup = tr.getRouter().getNodeGroupLink().getSourceNodeGroupId();
              String trTargetGroup = tr.getRouter().getNodeGroupLink().getTargetNodeGroupId();
              String nodeGroup = engine.getParameterService().getNodeGroupId();
              Trigger t = engine.getTriggerRouterService().getTriggerById(tr.getTriggerId(), false);
              String curChannel = t != null ? t.getChannelId() : "";
              String curReloadChannel = t != null ? t.getReloadChannelId() : "";
              if (ChannelType.OUTGOING.equals(type) && nodeGroup.equals(trSourceGroup)) {
                 if (curChannel.length() > 0 && !isHidableChannel(curChannel, engine)) {
                    channelList.add(curChannel);
                 }
  
                 if (curReloadChannel.length() > 0 && !isHidableChannel(curReloadChannel, engine)) {
                    channelList.add(curReloadChannel);
                 }
              } else if (ChannelType.INCOMING.equals(type) && nodeGroup.equals(trTargetGroup)) {
                 if (curChannel.length() > 0 && !isHidableChannel(curChannel, engine)) {
                    channelList.add(curChannel);
                 }
  
                 if (curReloadChannel.length() > 0 && !isHidableChannel(curReloadChannel, engine)) {
                    channelList.add(curReloadChannel);
                 }
              } else if (ChannelType.BOTH.equals(type)) {
                 if (curChannel.length() > 0 && !isHidableChannel(curChannel, engine)) {
                    channelList.add(curChannel);
                 }
  
                 if (curReloadChannel.length() > 0 && !isHidableChannel(curReloadChannel, engine)) {
                    channelList.add(curReloadChannel);
                 }
              }
           }
  
           channelList.add("config");
           channels = new ArrayList<>(channelList);
        } else {
           channels = toChannelIdList(engine.getConfigurationService().getNodeChannels(false));
        }
  
        Collections.sort(channels);
        return channels;
     }
    
     private boolean isHidableChannel(String channelId, ISymmetricEngine engine) {
        return engine.getParameterService().is("console.web.hide.system.info")
           && (channelId == null || channelId.equals("heartbeat") || channelId.equals("dynamic") || channelId.equals("0"));
     }

    private List<String> toChannelIdList(List<NodeChannel> channels) {
        List<String> channelIds = new ArrayList<>(channels.size());
  
        for (NodeChannel nodeChannel : channels) {
           channelIds.add(nodeChannel.getChannelId());
        }
  
        return channelIds;
     }     
}