import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';

import NodeStatus from 'Frontend/generated/com/jumpmind/symmetric/console/model/NodeStatus';

import { Table, Tooltip } from "antd";
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
          return (<Tooltip title="warning">
            <Icon icon="vaadin:warning" />
          </Tooltip>);
        }
        else if (text === '4') {
          return   (<Tooltip title="OK">
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
          render: (_: any, record: NodeStatus) => {
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
          render: (_: any, record: NodeStatus) => {
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
        },
      ],

    },

  ];
  

  return (
    <>
      <Table<NodeStatus> dataSource={nodes.filter((node): node is NodeStatus => node !== undefined)} columns={columns} />;
    </>
  );
}
