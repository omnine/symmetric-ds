import { Button } from '@vaadin/react-components/Button.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import {Grid} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';
import { HelloReactEndpoint } from 'Frontend/generated/endpoints.js';
import { useState } from 'react';
import VNNode from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/VNNode';

export default function HelloReactView() {
  const [name, setName] = useState('');
//  const nodes = useSignal<VNNode[]>([]);
  const [nodes, setNodes] = useState<(VNNode | undefined)[]>([]);
  return (
    <>
      <section className="flex p-m gap-m items-end">
        {/*
        <TextField
          label="Your name"
          onValueChanged={(e) => {
            setName(e.detail.value);
          }}
        />

        <Button
          onClick={async () => {
            const serverResponse = await HelloReactEndpoint.sayHello(name);
            Notification.show(serverResponse);
          }}
        >
          Say hello
        </Button>
        */}
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
        <GridColumn path="name" resizable />
        <GridColumn path="syncUrl" resizable />
      </Grid>

    </>
  );
}
