import {useEffect, useState} from "react";
import {ProAPIEndpoint} from "Frontend/generated/endpoints";


import {Grid} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';

import { Icon } from '@vaadin/react-components/Icon.js';

import MultiResult from "Frontend/generated/com/jumpmind/symmetric/console/ui/data/MultiResult";
import { Tooltip } from "@vaadin/react-components/Tooltip.js";


export default function MonitorsView() {
    const [multiResult, setMultiResult] = useState<MultiResult | null>(null);

    useEffect(() => {
      ProAPIEndpoint.getMonitorEvents().then(mr => setMultiResult(mr));
    }, []);

    const drawItem = (item:any, head:string) => {
      if(head === "nodeId") {
        return item.item[head].iconColor;
      }
//      console.log(item);
//      console.log(head);
      return (<Icon icon="vaadin:stop" style={{ color: item.item[head].iconColor }}/>);    
    }

    const tooltipGenerator = (context: any): string => {
      let text = '';
    
      const { column, item } = context;
      if (column && item) {
        if(column.path != 'nodeId') {
            text = item[column.path].tip;
        }
      }
    
      return text;
    };



    const drawMonitors = () => {
      if (!multiResult) return null;
      if(!multiResult.headers) return null;
      return (<Grid items={multiResult.rows} columnReorderingAllowed>
        <Tooltip slot="tooltip" generator={tooltipGenerator} />
          {multiResult.headers.map((head: string | undefined) => 
            head ? (
              <GridColumn header={head} path={head} key={head}>
                {(item) => drawItem(item, head)}
              </GridColumn>
            ) : null
          )}
      </Grid>);
    }


  return (
    <div className="flex flex-col h-full items-center justify-center p-l text-center box-border">
      {drawMonitors()}
    </div>
  );
}
