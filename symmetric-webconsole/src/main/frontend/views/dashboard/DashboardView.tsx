import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';

import { Card } from 'antd';
import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';
import HealthInfo from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HealthInfo';


import { Link } from 'react-router-dom';

export default function DashboardView() {
  const [name, setName] = useState('');
  const [healthInfo, setHealthInfo] = useState<HealthInfo | null>(null);

  useEffect(() => {
    ProAPIEndpoint.checkHealth().then(healthInfo => setHealthInfo(healthInfo));
  }, []);

  const renderHealthInfo = (hi: HealthInfo) => {
    return (
      <Card title="Health" bordered={false} style={{ width: 600 }}>
        <p>
          <Link to={`/nodes`}>
          {hi["totalOfflineNodes"]==0? <span>All Nodes Online <Icon icon="vaadin:check" /></span>:<span>{hi["totalOfflineNodes"]}  Offline Node <Icon icon="vaadin:warning" /></span>}
          </Link>
        </p>
        <p>
          {hi["totalIncomingErrors"]==0? <span>Incoming Batches OK <Icon icon="vaadin:check" /></span>:<span>{hi["totalIncomingErrors"]}  Incoming Error <Icon icon="vaadin:warning" /></span>}
        </p>
        <p>
          {hi["totalOutgoingErrors"]==0? <span>Outgoing Batches OK<Icon icon="vaadin:check" /></span>:<span>{hi["totalOutgoingErrors"]}  Outgoing Error <Icon icon="vaadin:warning" /></span>}
        </p>
        <p>
          <Link to={`/monitors`}>
            {hi["totalFailedMonitors"]==0? <span>All Monitors OK<Icon icon="vaadin:check" /></span>:<span>{hi["totalFailedMonitors"]} Monitor(s) Fired <Icon icon="vaadin:warning" /></span>}
          </Link> 
        </p>

     
      </Card>);
  }

  return (
    <>
      {healthInfo && renderHealthInfo(healthInfo)}
    </>
  );
}
