import {Grid, GridEventContext} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';
import {GridSelectionColumn} from '@vaadin/react-components/GridSelectionColumn';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';

import NodeStatus from 'Frontend/generated/com/jumpmind/symmetric/console/model/NodeStatus';


import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';
import { Tooltip } from '@vaadin/react-components/Tooltip.js';
import HealthInfo from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HealthInfo';
import { ProgressBar } from '@vaadin/react-components/ProgressBar.js';
import HillaOutgoingBatch from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HillaOutgoingBatch';

export default function BatchesView() {
  const [healthInfo, setHealthInfo] = useState<HealthInfo | null>(null);
  const [nodes, setNodes] = useState<(NodeStatus | undefined)[]>([]);
  useEffect(() => {
    ProAPIEndpoint.checkHealth().then(healthInfo => setHealthInfo(healthInfo));
    ProAPIEndpoint.listNodes().then(nodes => setNodes(nodes));
  }, []);




  const progressRenderer = (item: HillaOutgoingBatch) => {
    if (item.percent == -1) {
        return <Icon icon="vaadin:check" style={{ color: "#81C784" }} />;

    } else if (item.percent == -2) {
       return <Icon icon="vaadin:warning" style={{ color: "#E53935" }} />;
    }
    else if(item.percent == -3){
      return <Icon icon="vaadin:close" style={{ color: "#E53935" }} />;
    }
    else if(item.percent == -4){
      return <Icon icon="vaadin:hourglass-start" style={{ color: "#000000" }} />;
    }
    else {
      let percent = item.percent;
      if (percent > 100.0) {
          percent = 100.0;
//             this.a.debug("Loading percent > 100: processedRowCount = %s / dataRowCount = %s", processedRowCount, item.getDataRowCount());
      }

      return <><ProgressBar value={percent/100} /> {percent}%</>;

    }
   }
  



  return (
    <>
		  <h2>Outgoing Batches</h2>
      <Grid items={nodes} columnReorderingAllowed>
        <GridSelectionColumn />
        <GridColumn path="nodeId" header="Node" resizable />
        <GridColumn path="batchId" header="Batch ID" resizable>
        </GridColumn>
        <GridColumn path="outgoingDataCountRemaining" header="Progress">
        {({ item }) => progressRenderer(item)}
        </GridColumn>
        <GridColumn path="failedLineNumber" header="Failed Line Number">
        </GridColumn>
        <GridColumn path="bulkLoaderFlag" header="Bulk Loaded"/>
      </Grid>
		  <h2>Incoming Batches</h2>	  


    </>
  );
}
