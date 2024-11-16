import { Button } from '@vaadin/react-components/Button.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import {Grid} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';
import {GridSelectionColumn} from '@vaadin/react-components/GridSelectionColumn';
import { HelloReactEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';
import VNNode from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/VNNode';
import { Details } from '@vaadin/react-components/Details.js';
import { VerticalLayout } from '@vaadin/react-components/VerticalLayout.js';

import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';
import HealthInfo from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HealthInfo';

export default function HelloReactView() {
  const [name, setName] = useState('');
  const [healthInfo, setHealthInfo] = useState<HealthInfo | null>(null);
//  const nodes = useSignal<VNNode[]>([]);
  const [nodes, setNodes] = useState<(VNNode | undefined)[]>([]);
  useEffect(() => {
    HelloReactEndpoint.checkHealth().then(healthInfo => setHealthInfo(healthInfo));
  });

  const renderHealthInfo = (hi: HealthInfo) => {
    return (<Details summary="Health" opened>
      <VerticalLayout>
        {hi["totalOfflineNodes"]==0? <span>All Nodes Online <Icon icon="vaadin:check" /></span>:<span>{hi["totalOfflineNodes"]}  Offline Node <Icon icon="vaadin:warning" /></span>}
        {hi["totalIncomingErrors"]==0? <span>Incoming Batches OK <Icon icon="vaadin:check" /></span>:<span>{hi["totalIncomingErrors"]}  Incoming Error <Icon icon="vaadin:warning" /></span>}
        {hi["totalOutgoingErrors"]==0? <span>Outgoing Batches OK<Icon icon="vaadin:check" /></span>:<span>{hi["totalOutgoingErrors"]}  Outgoing Error <Icon icon="vaadin:warning" /></span>}
      </VerticalLayout>
    </Details>);
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
        <GridColumn path="name" resizable />
        <GridColumn path="syncUrl" resizable />
      </Grid>

    </>
  );
}
