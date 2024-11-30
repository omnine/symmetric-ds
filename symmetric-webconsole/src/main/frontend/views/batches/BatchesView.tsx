import {Grid} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';
import {GridSelectionColumn} from '@vaadin/react-components/GridSelectionColumn';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';

import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';

import { ProgressBar } from '@vaadin/react-components/ProgressBar.js';
import HillaBatch from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HillaBatch';

export default function BatchesView() {

  const [outgoingBatches, setOutgoingBatches] = useState<(HillaBatch | undefined)[]>([]);
  const [incomingBatches, setIncomingBatches] = useState<(HillaBatch | undefined)[]>([]);

  useEffect(() => {

    ProAPIEndpoint.getOutgoingBatches().then(batches => setOutgoingBatches(batches || []));
    ProAPIEndpoint.getIncomingBatches().then(batches => setIncomingBatches(batches || []));
  }, []);




  const progressRenderer = (item: HillaBatch) => {
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
    else if(item.percent == -5){
      return <span>N/A</span>;
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
  
   const nodeRenderer = (item: HillaBatch) => {
    if(item.nodeId == "-1"){
      return "Unrouted";
    }
    return item.nodeId;
   }


  return (
    <>
		  <h2>Outgoing Batches</h2>
      <Grid items={outgoingBatches} columnReorderingAllowed>
        <GridSelectionColumn />
        <GridColumn path="nodeId" header="Node" resizable>
        {({ item }) => nodeRenderer(item)}          
        </GridColumn>
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
      <Grid items={incomingBatches} columnReorderingAllowed>
        <GridSelectionColumn />
        <GridColumn path="nodeId" header="Node" resizable>
        {({ item }) => nodeRenderer(item)}          
        </GridColumn>
        <GridColumn path="batchId" header="Batch ID" resizable>

        </GridColumn>
        <GridColumn path="summaryShort" header="Table(s)" resizable />
        <GridColumn path="outgoingDataCountRemaining" header="Progress">
        {({ item }) => progressRenderer(item)}
        </GridColumn>
        <GridColumn path="failedLineNumber" header="Failed Line Number">
        </GridColumn>
        <GridColumn path="bulkLoaderFlag" header="Bulk Loaded"/>
      </Grid>

    </>
  );
}
