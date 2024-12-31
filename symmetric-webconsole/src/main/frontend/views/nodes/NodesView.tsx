import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';


import { Table, Tooltip } from "antd";
import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';
import { i18n } from "@lingui/core";

import HealthInfo from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HealthInfo';
import HillaNodeStatus from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HillaNodeStatus';

export default function NodesView() {
  i18n.activate("en");  //todo should move to root

  const [healthInfo, setHealthInfo] = useState<HealthInfo | null>(null);
  const [nodes, setNodes] = useState<(HillaNodeStatus | undefined)[]>([]);
  useEffect(() => {
    ProAPIEndpoint.checkHealth().then(healthInfo => setHealthInfo(healthInfo));
    ProAPIEndpoint.listNodes().then(nodes => setNodes(nodes));
  }, []);
 
  const columns = [
    {
      title: 'Node',
      dataIndex: 'nodeId',
      key: 'nodeId',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (text: string) => {
        if (text === '1') {
          return (<Tooltip title="Warning">
            <Icon icon="vaadin:warning" />
          </Tooltip>);
        }
        else if (text === '4') {
          return   (<Tooltip title="Good">
            <Icon icon="vaadin:check" />
          </Tooltip>);
        }
        return   (<Tooltip title="Disconnected">
          <Icon icon="vaadin:unlink" />
        </Tooltip>);
      }
    },
    {
      title: 'Outgoing',
      children: [
        {
          title: 'Rows',
          dataIndex: 'outgoingDataCountRemaining',
          key: 'outgoingDataCountRemaining',
          render: (_: any, record: HillaNodeStatus) => {
            if (healthInfo && record.nodeId === healthInfo.engineNodeId) {
              return "N/A";
            }
            return <>{record.outgoingDataCountRemaining}</>;
          }          

        },
        {
          title: 'Batches',
          dataIndex: 'outgoingBatchCountRemaining',
          key: 'outgoingBatchCountRemaining',
          render: (_: any, record: HillaNodeStatus) => {
            if (healthInfo && record.nodeId === healthInfo.engineNodeId) {
              return "N/A";
            }
            return <>{record.outgoingBatchCountRemaining}</>;
          }
        },
        {
          title: 'Last',
          dataIndex: 'lastOutgoingTime',
          key: 'lastOutgoingTime',
          render: (text: string) => {
            if ((text === 'N/A') || (text === '-')) {
              return text;
            }
            else{
              const d = new Date(text);
              return i18n.date(d, { dateStyle: "medium", timeStyle: "medium" });
            }

          }          
        },                
      ],
    },
    {
      title: 'Incoming',
      children: [
        {
          title: 'Rows',
          dataIndex: 'incomingDataCountRemaining',
          key: 'incomingDataCountRemaining',
        },
        {
          title: 'Batches',
          dataIndex: 'incomingBatchCountRemaining',
          key: 'incomingBatchCountRemaining',
        },
        {
          title: 'Last',
          dataIndex: 'lastIncomingTime',
          key: 'lastIncomingTime',
          render: (text: string) => {
            if ((text === 'N/A') || (text === '-')) {
              return text;
            }
            else{
              const d = new Date(text);
              return i18n.date(d, { dateStyle: "medium", timeStyle: "medium" });
            }

          }           
        },
      ],

    },

  ];
  

  return (
    <>
      <Table<HillaNodeStatus> dataSource={nodes.filter((node): node is HillaNodeStatus => node !== undefined)} columns={columns} />
      <div>
        Note: Dash "-" means no data. N/A means not applicable.
      </div> 
    </>
  );
}
