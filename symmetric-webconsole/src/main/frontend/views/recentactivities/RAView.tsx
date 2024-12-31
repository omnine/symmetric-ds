import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';


import RecentActivity from 'Frontend/generated/com/jumpmind/symmetric/console/model/RecentActivity';


import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';

import { Circles } from 'react-loader-spinner'
import { Table } from 'antd';
import { useLingui } from "@lingui/react"

export default function RAView() {
  const { i18n } = useLingui();
  const [recentActivities, setRecentActivities] = useState<RecentActivity[] | null>(null);


  useEffect(() => {

    ProAPIEndpoint.getRecentActivities().then(activities => {
      if (activities) {
        setRecentActivities(activities.filter((activity): activity is RecentActivity => activity !== undefined));
      } else {
        setRecentActivities([]);
      }
    });
  }, []);

  const columns = [
    {
      title: 'Status',
      dataIndex: 'running',
      key: 'running',
      render: (_: any, record: RecentActivity) => {
        if (record.running) {
          return (<Circles
            height={40}
            width={40}
            color="#4fa94d"
            ariaLabel="circles-loading"
            wrapperStyle={{}}
            wrapperClass=""
            visible={true}
          />);
        }
        return <Icon icon="vaadin:check" />;
      },
    },
    {
      title: 'Activity',
      dataIndex: 'message',
      key: 'message',
    },
    {
      title: 'When',
      dataIndex: 'endTime',
      key: 'endTime',
      render:(value: string) => {
        const d = new Date(value);
        return i18n.date(d, { dateStyle: "medium", timeStyle: "medium" });
      }
    },        
  ];

  const drawRecentActivities = (activities: RecentActivity[]) => {
    return (<Table<RecentActivity> dataSource={activities} columns={columns} />);
  }


  return (
    <>
      <h5>Processes that have worked on at least one row of data</h5>
      {recentActivities && drawRecentActivities(recentActivities)}
    </>
  );
}
