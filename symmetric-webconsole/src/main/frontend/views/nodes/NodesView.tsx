import {Grid} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';
import {GridColumnGroup} from '@vaadin/react-components/GridColumnGroup';
import {GridSelectionColumn} from '@vaadin/react-components/GridSelectionColumn';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';

import NodeStatus from 'Frontend/generated/com/jumpmind/symmetric/console/model/NodeStatus';


import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';
import HealthInfo from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HealthInfo';

export default function NodesView() {
  const [healthInfo, setHealthInfo] = useState<HealthInfo | null>(null);
  const [nodes, setNodes] = useState<(NodeStatus | undefined)[]>([]);
  useEffect(() => {
    ProAPIEndpoint.checkHealth().then(healthInfo => setHealthInfo(healthInfo));
    ProAPIEndpoint.listNodes().then(nodes => setNodes(nodes));
  }, []);


  const statusRenderer = (ns: NodeStatus) => {
    if (ns.status === '1') {
      return <Icon icon="vaadin:warning" />;
    }
    else if (ns.status === '4') {
      return <Icon icon="vaadin:check" />;
    }
    return <Icon icon="vaadin:unlink" />;

  }

  const outRowRenderer = (ns: NodeStatus) => {
    if (healthInfo && ns.nodeId === healthInfo.engineNodeId) {
      return "N/A";
    }
    return <>{ns.outgoingDataCountRemaining}</>;
  }

  const outBatchRenderer = (ns: NodeStatus) => {
    if (healthInfo && ns.nodeId === healthInfo.engineNodeId) {
      return "N/A";
    }
    return <>{ns.outgoingBatchCountRemaining}</>;
  }


  return (
    <>
      <Grid items={nodes} columnReorderingAllowed>
        <GridSelectionColumn />
        <GridColumn path="nodeId" header="Node" resizable />
        <GridColumn path="status" header="Status" resizable>
          {({ item }) => statusRenderer(item)}
        </GridColumn>

        <GridColumnGroup header="Outgoing">
          <GridColumn path="outgoingDataCountRemaining" header="Rows">
            {({ item }) => outRowRenderer(item)}
          </GridColumn>
          <GridColumn path="outgoingBatchCountRemaining" header="Batches">
            {({ item }) => outBatchRenderer(item)}            
          </GridColumn>
          <GridColumn path="lastOutgoingTime" header="Last"/>
        </GridColumnGroup>
        <GridColumnGroup header="Incoming">
          <GridColumn path="incomingDataCountRemaining" header="Rows"/>
          <GridColumn path="incomingBatchCountRemaining" header="Batches"/>
          <GridColumn path="lastIncomingTime" header="Last"/>
        </GridColumnGroup>
      </Grid>

    </>
  );
}
