import {useEffect, useState} from "react";
import {ProAPIEndpoint} from "Frontend/generated/endpoints";

import { Icon } from '@vaadin/react-components/Icon.js';

import MultiResult from "Frontend/generated/com/jumpmind/symmetric/console/ui/data/MultiResult";

import { Table } from "antd";


export default function MonitorsView() {
    const [multiResult, setMultiResult] = useState<MultiResult | null>(null);

    useEffect(() => {
      ProAPIEndpoint.getMonitorEvents().then(mr => setMultiResult(mr));
    }, []);

    const drawItem = (record:any, head:string) => {
      if(head === "node-0") {
        return record[head].iconColor;
      }
//      console.log(item);
//      console.log(head);
      return (<Icon icon="vaadin:stop" style={{ color: record[head].iconColor }}/>);    
    }



    const buildColumns = () => {
      if (!multiResult) return [];
      if(!multiResult.headers) return [];
      return multiResult.headers
        .map((head: string | undefined) => 
          head ? (
            {
              title: head=="node-0"? "Monitor": head,
              dataIndex: head,
              key: head,
              render: (_: any, record: any) => {
                return drawItem(record, head);
              }
            }
          ) : null
        )
        .filter(column => column !== null);
    }


    const drawMonitors = () => {
      if (!multiResult) return null;
      if(!multiResult.headers) return null;
      return (<Table dataSource={multiResult.rows as any} columns={buildColumns()} />);


    }


  return (
    <div className="flex flex-col h-full items-center justify-center p-l text-center box-border">
      {drawMonitors()}
    </div>
  );
}
