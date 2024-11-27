import {Grid} from '@vaadin/react-components/Grid';
import {GridColumn} from '@vaadin/react-components/GridColumn';
import {GridColumnGroup} from '@vaadin/react-components/GridColumnGroup';
import {GridSelectionColumn} from '@vaadin/react-components/GridSelectionColumn';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import { useState, useEffect } from 'react';

import NodeStatus from 'Frontend/generated/com/jumpmind/symmetric/console/model/NodeStatus';
import RecentActivity from 'Frontend/generated/com/jumpmind/symmetric/console/model/RecentActivity';


import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';


import { Circles } from 'react-loader-spinner'


export default function RAView() {
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


  const drawRecentActivities = (activities: RecentActivity[]) => {
    return (<Grid items={activities} columnReorderingAllowed>
      <GridColumn path="running" header="Status" resizable>
        {({ item }) => item.running ? (<Circles
                      height="40"
                      width="40"
                      color="#4fa94d"
                      ariaLabel="circles-loading"
                      wrapperStyle={{}}
                      wrapperClass=""
                      visible={true}
                      />) : <Icon icon="vaadin:check" />}
      </GridColumn>
      <GridColumn path="message" header="Activity" resizable />
      <GridColumn path="endTime" header="When" resizable>

      </GridColumn>
    </Grid>);
  }


  return (
    <>
      {recentActivities && drawRecentActivities(recentActivities)}
    </>
  );
}
