import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';

import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';

import { ProgressBar } from '@vaadin/react-components/ProgressBar.js';
import HillaBatch from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HillaBatch';
import { Table } from 'antd';

export default function BatchesView() {
  const [outgoingSummary, setOutgoingSummary] = useState<string>("");
  const [incomingSummary, setIncomingSummary] = useState<string>("");

  const [outgoingBatches, setOutgoingBatches] = useState<(HillaBatch | undefined)[]>([]);
  const [incomingBatches, setIncomingBatches] = useState<(HillaBatch | undefined)[]>([]);

  useEffect(() => {
    ProAPIEndpoint.getOutgoingBatchSummary().then((summary: string | undefined) => setOutgoingSummary(summary || ""));
    ProAPIEndpoint.getIncomingBatchSummary().then((summary: string | undefined) => setIncomingSummary(summary || ""));

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

   const outgoingColumns = [
    {
      title: 'Node',
      dataIndex: 'nodeId',
      key: 'nodeId',
      render: (text: string) => {
        if (text === '-1') {
          return "Unrouted";
        }
        return text;
      }
    },
    {
      title: 'Batch ID',
      dataIndex: 'batchId',
      key: 'batchId',
    },
    {
      title: 'Progress',
      dataIndex: 'percent',
      key: 'percent',
      render: (_: any, record: HillaBatch) => {
        return progressRenderer(record);
      }
    },
    {
      title: 'Failed Line Number',
      dataIndex: 'failedLineNumber',
      key: 'failedLineNumber',
    },
    {
      title: 'Bulk Loaded',
      dataIndex: 'bulkLoaderFlag',
      key: 'bulkLoaderFlag',
    },           
  ];

  return (
    <>
		  <h4>Outgoing Batches</h4>
      <h5>{outgoingSummary}</h5>
      <Table<HillaBatch> dataSource={outgoingBatches.filter(batch => batch !== undefined) as HillaBatch[]} columns={outgoingColumns} />
		  <h3>Incoming Batches</h3>
      <h4>{incomingSummary}</h4>
      <Table<HillaBatch> dataSource={incomingBatches.filter(batch => batch !== undefined) as HillaBatch[]} columns={outgoingColumns} />     
    </>
  );
}
