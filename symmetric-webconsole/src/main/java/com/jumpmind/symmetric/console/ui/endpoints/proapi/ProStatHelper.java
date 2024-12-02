package com.jumpmind.symmetric.console.ui.endpoints.proapi;

import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.service.IStatisticService;
import org.jumpmind.symmetric.statistic.ChannelStats;
import org.jumpmind.symmetric.statistic.IStatisticManager;
import org.jumpmind.symmetric.model.Node;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class ProStatHelper {
    private String nodeId;
    private Date minDate;

    public int getScaleFactor(Integer period, boolean isDay) {
        int scaleFactor = 60;
        if (isDay) {
            scaleFactor = 1440;
        }

        return period * scaleFactor;
    }

    public String findNodeId(ISymmetricEngine engine) {
        Node node = engine.getNodeService().getCachedIdentity();
        return node != null ? node.getNodeId() : null;
    }

    public TreeMap<Date, Map<String, ChannelStats>> getNodeStatsForPeriod(ISymmetricEngine engine) {
        int nodeStatsPeriodInMinutes = getScaleFactor(1, false);    // last 1 hour, get from front-end
        IStatisticService statisticsService = engine.getStatisticService();
        IStatisticManager statisticManager = engine.getStatisticManager();
        INodeService nodeService = engine.getNodeService();
        nodeId = findNodeId(engine);
        minDate = statisticsService.getMinNodeStats(nodeId);
        Calendar startTime = Calendar.getInstance();
        startTime.add(12, -nodeStatsPeriodInMinutes);
        Calendar endTime = Calendar.getInstance();
        int interval = nodeStatsPeriodInMinutes / 120 * 5;
        return statisticManager.getNodeStatsForPeriod(startTime.getTime(), endTime.getTime(), nodeService.findIdentityNodeId(), interval > 0 ? interval : 5);
    }
}
