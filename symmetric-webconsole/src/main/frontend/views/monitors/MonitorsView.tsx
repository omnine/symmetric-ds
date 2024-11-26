import {useEffect, useState} from "react";
import {HelloReactEndpoint} from "Frontend/generated/endpoints";


import {Grid} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';
import {GridColumnGroup} from '@vaadin/react-components/GridColumnGroup';
import {GridSelectionColumn} from '@vaadin/react-components/GridSelectionColumn';
import { Icon } from '@vaadin/react-components/Icon.js';
import MonitorEvent from "Frontend/generated/com/jumpmind/symmetric/console/model/MonitorEvent";
import Monitor from "Frontend/generated/com/jumpmind/symmetric/console/model/Monitor";
import MultiResult from "Frontend/generated/com/jumpmind/symmetric/console/ui/endpoints/helloreact/MultiResult";

export default function MonitorsView() {
    const [multiResult, setMultiResult] = useState<MultiResult | null>(null);

    useEffect(() => {
        HelloReactEndpoint.getMonitorEvents().then(mr => setMultiResult(mr));
    }, []);

    const drawItem = ({item, monitor}: {item: any, monitor: Monitor}) => {
       const me:MonitorEvent = item.getMonitorEvents().get(monitor.type);

      var iconColor = "#77DD76";
      if (me != null && !me.resolved) {
         if (me.severityLevel == 100) {
            iconColor ="#FFD700";
         } else if (me.severityLevel == 200) {
            iconColor = "#f39c12";
         } else if (me.severityLevel == 300) {
            iconColor = "#FF6962";
         }
      }


      return (<Icon icon="vaadin:stop" style={{ color: iconColor }}/>);    
    }


    const drawMonitors = () => {
      if (!multiResult) {
        return null;
      }
      return (<Grid items={multiResult.nodeMonitors} columnReorderingAllowed>
        <GridColumn path="nodeId" header="nodeId" resizable />
        {multiResult.monitors && multiResult.monitors.filter((monitor): monitor is Monitor => monitor !== undefined).map((monitor: Monitor) => (
            <GridColumn>
              {({ item }) => drawItem({item, monitor})}
            </GridColumn>
      ))}


      </Grid>);
    }


  return (
    <div className="flex flex-col h-full items-center justify-center p-l text-center box-border">
      {drawMonitors()}
    </div>
  );
}
