package com.jumpmind.symmetric.console.ui.endpoints.proapi;

import com.jumpmind.symmetric.console.model.Monitor;
import com.jumpmind.symmetric.console.model.NodeMonitors;
import com.jumpmind.symmetric.console.model.RecentActivity;
import com.jumpmind.symmetric.console.remote.BatchStatus;
import com.jumpmind.symmetric.console.remote.IBatchStatusService;
import com.jumpmind.symmetric.console.service.IMonitorService;
import com.jumpmind.symmetric.console.service.impl.MonitorService;
import com.jumpmind.symmetric.console.ui.data.HillaBatch;
import com.jumpmind.symmetric.console.ui.data.MixedIncomingStatus;
import com.jumpmind.symmetric.console.ui.data.VNNode;
import com.jumpmind.symmetric.console.ui.data.HealthInfo;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
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
    private ProEngineHelper proEngineHelper;
    // Constructor
    public ProAPIEndpoint() {
        this.proEngineHelper = new ProEngineHelper();
    }

    public String getOutgoingBatchSummary() {
        ISymmetricEngine engine = proEngineHelper.getSymmetricEngine();
        Map<NodeGroupLink, Set<String>> activeConfiguration = getActiveConfiguration(engine);
        return outgoingBatchSummary(engine, activeConfiguration);
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
        ISymmetricEngine engine = proEngineHelper.getSymmetricEngine();       
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

        ISymmetricEngine engine = proEngineHelper.getSymmetricEngine();

        IOutgoingBatchService outgoingService = engine.getOutgoingBatchService();
        IIncomingBatchService incomingService = engine.getIncomingBatchService();
        
        List<String> channels = getConsoleDisplayChannelIds(engine, ChannelType.BOTH);


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
        Set<String> setChannels = new HashSet<>(
                getConsoleDisplayChannelIds(engine, ChannelType.BOTH)
        );

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

     public List<HillaBatch> getOutgoingBatches() {
        Map<String, Long> blockedChannels = new HashMap<>();

        ISymmetricEngine engine = proEngineHelper.getSymmetricEngine();
        List<OutgoingBatch> outgoingBatches = listOutgoingBatches(engine);
        List<HillaBatch> hillaOutgoingBatches = new ArrayList<>();
        for (OutgoingBatch outgoingBatch : outgoingBatches) {
            HillaBatch hillaBatch = new HillaBatch();
            hillaBatch.channelId = outgoingBatch.getChannelId();
            hillaBatch.nodeId = outgoingBatch.getNodeId();
            hillaBatch.batchId = Long.parseLong(outgoingBatch.getNodeBatchId());
            hillaBatch.summary = outgoingBatch.getSummary();
            hillaBatch.failedLineNumber = outgoingBatch.getFailedLineNumber();
            hillaBatch.bulkLoaderFlag = outgoingBatch.isBulkLoaderFlag();
            hillaBatch.errorFlag = outgoingBatch.isErrorFlag();
            hillaBatch.processedRowCount = outgoingBatch.getProcessedRowCount();
            hillaBatch.status = outgoingBatch.getStatus().toString();


//                  AbstractBatch value = this.a(outgoingBatch.toString());
                  long processedRowCount = outgoingBatch.getProcessedRowCount();
                  double percent = (double)processedRowCount / (double)outgoingBatch.getDataRowCount() * 100.0;
                  String status = outgoingBatch.getStatus().toString();
                  if (status.equals(Status.OK.toString()) || status.equals(Status.IG.toString())) {
                     Long blockedBatchId = blockedChannels.get(outgoingBatch.getNodeBatchId());
                     if (blockedBatchId != null && outgoingBatch.getBatchId() > blockedBatchId) {
                        blockedChannels.remove(outgoingBatch.getNodeId() + "-" + outgoingBatch.getChannelId());
                     }

                     hillaBatch.percent = -1;


                  } else if (outgoingBatch.isErrorFlag()) {
                     String channel = outgoingBatch.getChannelId();
                     Long batchId = outgoingBatch.getBatchId();
                     String nodeId = outgoingBatch.getNodeId();
                     blockedChannels.put(nodeId + "-" + channel, batchId);
                     hillaBatch.percent = -2;
                  } else {
                     Long batchId = outgoingBatch.getBatchId();
                     String channel = outgoingBatch.getChannelId();
                     String nodeId = outgoingBatch.getNodeId();
                     Long blockedBatchId = blockedChannels.get(nodeId + "-" + channel);
                     if (blockedBatchId != null && batchId > blockedBatchId) {
                        hillaBatch.percent = -3;
                     } else if (processedRowCount == 0L) {
                        hillaBatch.percent = -4;
                     } else {
                        if (percent > 100.0) {
                           percent = 100.0;
//                           this.a.debug("Loading percent > 100: processedRowCount = %s / dataRowCount = %s", processedRowCount, item.getDataRowCount());
                        }

                     }
                  }



            hillaOutgoingBatches.add(hillaBatch);
        }
        return hillaOutgoingBatches;
     }

     private List<OutgoingBatch> listOutgoingBatches(ISymmetricEngine engine) {
        IOutgoingBatchService service = engine.getOutgoingBatchService();
        IBatchStatusService batchStatusService = engine
           .getExtensionService()
           .getExtensionPoint(IBatchStatusService.class);

         List<String> channels = new ArrayList<>();

         for (Channel channel : engine.getConfigurationService().getChannels(false).values()) {
             if (!channel.getChannelId().equals("config") && !channel.getChannelId().equals("heartbeat")) {
                 channels.add(channel.getChannelId());
             }
         }


        List<OutgoingBatch> batchList = new ArrayList<>();

        List<String> nodeIds = new ArrayList<>();
        List<Status> statusQuery = new ArrayList<>();
         List<Long> loads = new ArrayList<>(0);
         List<Status> statusErrors = new ArrayList<>();
         statusQuery.add(Status.QY);
         statusErrors.add(Status.ER);

        List<OutgoingBatch> inProcessBatchList = service.listOutgoingBatches(nodeIds, channels, statusQuery, loads, -1L, null, -1, true);
        List<OutgoingBatch> tempErrorBatchList = service.listOutgoingBatches(nodeIds, channels, statusErrors, loads, -1L, null, -1, false);
        List<OutgoingBatch> errorBatchList = new ArrayList<>();
  
        for (OutgoingBatch b : tempErrorBatchList) {
           if (b.getStatus().equals(Status.ER)
              && (batchStatusService.getLatestStatus(b) == null || !batchStatusService.getLatestStatus(b).getStatus().equals(Status.OK.name()))) {
              errorBatchList.add(b);
           }
        }
  
        Set<String> nodeChannelErrors = new HashSet<>();
  
        for (OutgoingBatch batch : errorBatchList) {
           nodeChannelErrors.add(batch.getNodeId() + batch.getChannelId());
        }
  
        for (OutgoingBatch batch : inProcessBatchList) {
           if (!nodeChannelErrors.contains(batch.getNodeId() + batch.getChannelId())
              && !batch.getNodeId().equals("-1")
              && (batchStatusService.getLatestStatus(batch) == null || !batchStatusService.getLatestStatus(batch).getStatus().equals(Status.OK.name()))) {
              batchList.add(batch);
           }
        }

        List<OutgoingBatch> ob = new ArrayList<>();
        Map<String, OutgoingBatch> obMap = new HashMap<>();

        ob.addAll(batchList);
  
        for (OutgoingBatch bx : ob) {
           BatchStatus latestStatus = batchStatusService.getLatestStatus(bx);
           if (latestStatus == null) {
               obMap.put(bx.getNodeBatchId(), bx);
           } else {
              if (!latestStatus.getStatus().equals(Status.OK.name())) {
                  obMap.put(bx.getNodeBatchId(), bx);
              }
  
              bx.setStatusFromString(latestStatus.getStatus());
              bx.setProcessedRowCount(latestStatus.getProcessedRowCount());
              bx.setSummary(bx.getSummary());
           }
        }
  
        batchList.addAll(errorBatchList);
        return batchList;
     }

    private List<IncomingBatch> geIncomingBatches(ISymmetricEngine engine) {
        IIncomingBatchService service = engine.getIncomingBatchService();
        List<Status> notOkStatus = new ArrayList<>();

        notOkStatus.add(Status.QY);
        notOkStatus.add(Status.SE);
        notOkStatus.add(Status.LD);
        notOkStatus.add(Status.ER);

        List<String> channels = getConsoleDisplayChannelIds(engine, ChannelType.INCOMING);
        List<String> nodeIds = new ArrayList<>();
        List<Long> loads = new ArrayList<>(0);
        return service.listIncomingBatches(nodeIds, channels, notOkStatus, loads, null, null, -1, false);
    }

    // Incoming Batches part

    protected List<ProcessInfo> filterProcessInfo(List<ProcessInfo> processInfos) {
        List<ProcessInfo> batchProcessInfos = new ArrayList<>();
  
        for (ProcessInfo info : processInfos) {
           if (info.getCurrentBatchId() != 0L) {
              batchProcessInfos.add(info);
           }
        }
  
        return batchProcessInfos;
     }

     private Status process2Status(ProcessStatus status) {
        switch (status) {
           case NEW:
              return Status.NE;
           case PROCESSING:
           case TRANSFERRING:
           case LOADING:
           case ACKING:
           case QUERYING:
              return Status.LD;
           case ERROR:
              return Status.ER;
           case OK:
              return Status.OK;
           default:
              return Status.XX;
        }
     }     

    public List<HillaBatch> getIncomingBatches() {
        Map<String, Long> blockedChannels = new HashMap<>();

        ISymmetricEngine engine = proEngineHelper.getSymmetricEngine();

        Map<Long, MixedIncomingStatus> inBatchStatusMap = new HashMap<>();
        List<ProcessInfo> processInfos = filterProcessInfo(engine.getStatisticManager().getProcessInfos());

        List<IncomingBatch> allIncoming = geIncomingBatches(engine);
  
        for (IncomingBatch in : allIncoming) {
           boolean found = false;
  
           for (ProcessInfo info : processInfos) {
              if (info.getCurrentBatchId() == in.getBatchId()) {
                 String mappedStatus = process2Status(info.getStatus()).name();
                 inBatchStatusMap.put(in.getBatchId(), new MixedIncomingStatus(info.getCurrentBatchId(), mappedStatus, info.getCurrentDataCount(), in.getExtractRowCount()));
                 found = true;
                 break;
              }
           }
  
           if (!found) {
              inBatchStatusMap.put(
                 in.getBatchId(), new MixedIncomingStatus(in.getBatchId(), in.getStatus().name(), in.getLoadRowCount(), in.getExtractRowCount(), in.getSqlMessage())
              );
           }
        }



        List<OutgoingBatch> outgoingBatches = listOutgoingBatches(engine);
        List<HillaBatch> hillaOutgoingBatches = new ArrayList<>();
        for (OutgoingBatch outgoingBatch : outgoingBatches) {
            HillaBatch hillaBatch = new HillaBatch();
            hillaBatch.channelId = outgoingBatch.getChannelId();
            hillaBatch.nodeId = outgoingBatch.getNodeId();
            hillaBatch.batchId = Long.parseLong(outgoingBatch.getNodeBatchId());
            hillaBatch.summary = outgoingBatch.getSummary();
            hillaBatch.failedLineNumber = outgoingBatch.getFailedLineNumber();
            hillaBatch.bulkLoaderFlag = outgoingBatch.isBulkLoaderFlag();
            hillaBatch.errorFlag = outgoingBatch.isErrorFlag();
            hillaBatch.processedRowCount = outgoingBatch.getProcessedRowCount();
            hillaBatch.status = outgoingBatch.getStatus().toString();

            MixedIncomingStatus batchStatus = inBatchStatusMap.get(outgoingBatch.getBatchId());
            if (batchStatus != null) {
               String status = batchStatus.b;
               long processedRowCount = batchStatus.c;
               long totalRowCount = outgoingBatch.getDataRowCount();
               outgoingBatch.setProcessedRowCount(processedRowCount);
               outgoingBatch.setDataRowCount(totalRowCount);
               double percent = totalRowCount == 0L ? 0.0 : (double)processedRowCount / (double)totalRowCount * 100.0;
               if (status.equals(Status.OK.name()) || status.equals(Status.IG.name())) {
                hillaBatch.percent = -1;
               } else if (status.equals(Status.ER.name())) {
                hillaBatch.percent = -2;
               } else if (processedRowCount == 0L) {
                hillaBatch.percent = -4;
               } else {
                  if (percent > 100.0) {
                     percent = 100.0;
//                     this.a.debug("Loading percent > 100: processedRowCount = %s / dataRowCount = %s", processedRowCount, item.getDataRowCount());
                  }
                  hillaBatch.percent = (int)percent; 
               }
            } else {
                hillaBatch.percent = -5;
            }

        }
        return hillaOutgoingBatches;
    }

    private Map<NodeGroupLink, Set<String>> getActiveConfiguration(ISymmetricEngine engine) {
        List<TriggerRouter> triggerRouters = engine.getTriggerRouterService().getTriggerRouters(false);
        Map<NodeGroupLink, Set<String>> activeConfiguration = new HashMap<>();
        String groupId = engine.getParameterService().getString("group.id");
  
        for (TriggerRouter triggerRouter : triggerRouters) {
           Set<String> activeTables = activeConfiguration.get(triggerRouter.getRouter().getNodeGroupLink());
           if (activeTables == null) {
              activeTables = new HashSet<>();
           }
  
           String triggerRouterGroupId = triggerRouter.getRouter().getNodeGroupLink().getTargetNodeGroupId();
           if (triggerRouterGroupId.equals(groupId)) {
              activeTables.add(triggerRouter.getTrigger().getFullyQualifiedSourceTableName());
              activeConfiguration.put(triggerRouter.getRouter().getNodeGroupLink(), activeTables);
           }
        }
  
        return activeConfiguration;
     }

     private String incomingBatchSummary(ISymmetricEngine engine, Map<NodeGroupLink, Set<String>> activeConfiguration) {
        INodeService nodeService = engine.getNodeService();
        int totalNodesReplicatingWithPush = nodeService.findNodesWhoPushToMe().size();
        int totalTablesPush = 0;
        int totalNodesReplicatingWithPull = nodeService.findNodesToPull().size();
        int totalTablesPull = 0;
  
        for (Map.Entry<NodeGroupLink, Set<String>> entry : activeConfiguration.entrySet()) {
           NodeGroupLink link = entry.getKey();
           if (link.getTargetNodeGroupId().equals(engine.getParameterService().getNodeGroupId())) {
              if (link.getDataEventAction() == NodeGroupLinkAction.P) {
                 totalTablesPush += entry.getValue().size();
              } else if (link.getDataEventAction() == NodeGroupLinkAction.W) {
                 totalTablesPull += entry.getValue().size();
              }
           }
        }
  
        StringBuilder groupLinkAction = new StringBuilder();
        if (totalTablesPull > 0 || totalNodesReplicatingWithPull > 0) {
           groupLinkAction.append(totalTablesPull);
           if (totalTablesPull > 1) {
              groupLinkAction.append(" tables pulled from ");
           } else {
              groupLinkAction.append(" table pulled from ");
           }
  
           groupLinkAction.append(totalNodesReplicatingWithPull);
           if (totalNodesReplicatingWithPull > 1) {
              groupLinkAction.append(" nodes");
           } else {
              groupLinkAction.append(" node");
           }
  
           int pullSeconds = 0;
  
           try {
              pullSeconds = engine.getParameterService().getInt("job.pull.period.time.ms") / 1000;
           } catch (Exception var10) {
           }
  
           groupLinkAction.append(" every ").append(pullSeconds).append("s");
        }
  
        if (totalTablesPush > 0 || totalNodesReplicatingWithPush > 0) {
           groupLinkAction.append(totalTablesPush);
           if (totalTablesPush > 1) {
              groupLinkAction.append(" tables pushed to ");
           } else {
              groupLinkAction.append(" table pushed to ");
           }
  
           groupLinkAction.append(" this node from ");
           groupLinkAction.append(totalNodesReplicatingWithPush);
           if (totalNodesReplicatingWithPush > 1) {
              groupLinkAction.append(" nodes");
           } else {
              groupLinkAction.append(" node");
           }
  
           int pushSeconds = 0;
  
           try {
              pushSeconds = engine.getParameterService().getInt("job.push.period.time.ms") / 1000;
           } catch (Exception var9) {
           }
  
           groupLinkAction.append(" every ").append(pushSeconds).append("s");
        }
  
        return groupLinkAction.toString();
     }

     private String outgoingBatchSummary(ISymmetricEngine engine, Map<NodeGroupLink, Set<String>> activeConfiguration) {
         INodeService nodeService = engine.getNodeService();
        int totalNodesReplicatingWithPush = nodeService.findNodesToPushTo().size();
        int totalTablesPush = 0;
        int totalNodesReplicatingWithPull = nodeService.findNodesWhoPullFromMe().size();
        int totalTablesPull = 0;
  
        for (Map.Entry<NodeGroupLink, Set<String>> entry : activeConfiguration.entrySet()) {
           NodeGroupLink link = entry.getKey();
           if (link.getSourceNodeGroupId().equals(engine.getParameterService().getNodeGroupId())) {
              if (link.getDataEventAction() == NodeGroupLinkAction.P) {
                 totalTablesPush += entry.getValue().size();
              } else if (link.getDataEventAction() == NodeGroupLinkAction.W) {
                 totalTablesPull += entry.getValue().size();
              }
           }
        }
  
        StringBuilder groupLinkAction = new StringBuilder();
        if (totalTablesPull > 0 || totalNodesReplicatingWithPull > 0) {
           groupLinkAction.append(totalTablesPull);
           if (totalTablesPull > 1) {
              groupLinkAction.append(" tables sent to ");
           } else {
              groupLinkAction.append(" table sent to ");
           }
  
           groupLinkAction.append(totalNodesReplicatingWithPull);
           if (totalNodesReplicatingWithPull > 1) {
              groupLinkAction.append(" nodes when pulled");
           } else {
              groupLinkAction.append(" node when pulled");
           }
  
           int pullSeconds = 0;
  
           try {
              pullSeconds = engine.getParameterService().getInt("job.pull.period.time.ms") / 1000;
           } catch (Exception var10) {
           }
  
           groupLinkAction.append(" every ").append(pullSeconds).append("s");
        }
  
        if (totalTablesPush > 0 || totalNodesReplicatingWithPush > 0) {
           groupLinkAction.append("Push ").append(totalTablesPush);
           if (totalTablesPush > 1) {
              groupLinkAction.append(" tables to ");
           } else {
              groupLinkAction.append(" table to ");
           }
  
           groupLinkAction.append(totalNodesReplicatingWithPush);
           if (totalNodesReplicatingWithPush > 1) {
              groupLinkAction.append(" nodes");
           } else {
              groupLinkAction.append(" node");
           }
  
           int pushSeconds = 0;
  
           try {
              pushSeconds = engine.getParameterService().getInt("job.push.period.time.ms") / 1000;
           } catch (Exception var9) {
           }
  
           groupLinkAction.append(" every ").append(pushSeconds).append("s");
        }
  
        return groupLinkAction.toString();
     }
  

     public String getIncomingBatchSummary() {
        ISymmetricEngine engine = proEngineHelper.getSymmetricEngine();
        Map<NodeGroupLink, Set<String>> activeConfiguration = getActiveConfiguration(engine);
        return incomingBatchSummary(engine, activeConfiguration);
     }




}