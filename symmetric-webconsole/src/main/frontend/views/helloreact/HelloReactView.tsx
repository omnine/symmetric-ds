import { Button } from '@vaadin/react-components/Button.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import {Grid} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';
import {GridColumnGroup} from '@vaadin/react-components/GridColumnGroup';
import {GridSelectionColumn} from '@vaadin/react-components/GridSelectionColumn';
import { HelloReactEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';

import NodeStatus from 'Frontend/generated/com/jumpmind/symmetric/console/model/NodeStatus';
import RecentActivity from 'Frontend/generated/com/jumpmind/symmetric/console/model/RecentActivity';
import { Details } from '@vaadin/react-components/Details.js';
import { VerticalLayout } from '@vaadin/react-components/VerticalLayout.js';

import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';
import HealthInfo from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HealthInfo';

import { Circles } from 'react-loader-spinner'

export default function HelloReactView() {
  const [name, setName] = useState('');
  const [healthInfo, setHealthInfo] = useState<HealthInfo | null>(null);
//  const nodes = useSignal<VNNode[]>([]);
  const [nodes, setNodes] = useState<(NodeStatus | undefined)[]>([]);
  useEffect(() => {
    HelloReactEndpoint.checkHealth().then(healthInfo => setHealthInfo(healthInfo));
  }, []);

  const renderHealthInfo = (hi: HealthInfo) => {
    return (<Details summary="Health" opened>
      <VerticalLayout>
        {hi["totalOfflineNodes"]==0? <span>All Nodes Online <Icon icon="vaadin:check" /></span>:<span>{hi["totalOfflineNodes"]}  Offline Node <Icon icon="vaadin:warning" /></span>}
        {hi["totalIncomingErrors"]==0? <span>Incoming Batches OK <Icon icon="vaadin:check" /></span>:<span>{hi["totalIncomingErrors"]}  Incoming Error <Icon icon="vaadin:warning" /></span>}
        {hi["totalOutgoingErrors"]==0? <span>Outgoing Batches OK<Icon icon="vaadin:check" /></span>:<span>{hi["totalOutgoingErrors"]}  Outgoing Error <Icon icon="vaadin:warning" /></span>}
        {hi["totalFailedMonitors"]==0? <span>All Monitors OK<Icon icon="vaadin:check" /></span>:<span>{hi["totalFailedMonitors"]} Monitor(s) Fired <Icon icon="vaadin:warning" /></span>}
      </VerticalLayout>
    </Details>);
  }

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

  const drawRecentActivities = (activities: RecentActivity[]) => {
    return (<Grid items={activities} columnReorderingAllowed>
      <GridColumn path="running" header="Status" resizable>
        {({ item }) => item.running ? (<Circles
                      height="40"
                      width="40"
                      color="#4fa94d"
                      ariaLabel="circles-loading"
                      wrapperStyle={{}}
                      wrapperClass=""
                      visible={true}
                      />) : <Icon icon="vaadin:check" />}
      </GridColumn>
      <GridColumn path="message" header="Activity" resizable />
      <GridColumn path="endTime" header="When" resizable>
        {({ item }) => statusRenderer(item)}
      </GridColumn>
    </Grid>);
  }


  return (
    <>
      {healthInfo && renderHealthInfo(healthInfo)}
        <section className="flex p-m gap-m items-end">
        {/*
        <TextField
          label="Your name"
          onValueChanged={(e) => {
            setName(e.detail.value);
          }}
        />
        */}
        <Button
          onClick={async () => {
            const serverResponse = await HelloReactEndpoint.sayHello();
            Notification.show(serverResponse);
          }}
        >
          Say hello
        </Button>

          <Button
              onClick={async () => {
                  const nodes = await HelloReactEndpoint.listNodes();
                  setNodes(nodes);
              }}
          >
              List Nodes
          </Button>
      </section>
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
