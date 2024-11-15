import { Button } from '@vaadin/react-components/Button.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import {Grid} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';
import {GridSelectionColumn} from '@vaadin/react-components/GridSelectionColumn';
import { HelloReactEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';
import VNNode from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/VNNode';

export default function HelloReactView() {
  const [name, setName] = useState('');
  const [totalOfflineNodes, setTotalOfflineNodes] = useState(-1);  

//  const nodes = useSignal<VNNode[]>([]);
  const [nodes, setNodes] = useState<(VNNode | undefined)[]>([]);
  useEffect(() => {
    HelloReactEndpoint.getOfflineNodes().then(ton => setTotalOfflineNodes(ton));
  });
  
  return (
    <>
    <div>{totalOfflineNodes}</div>
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
