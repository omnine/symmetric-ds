import HillaStat from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HillaStat';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints';
import { useEffect, useState } from 'react';
import { ResponsiveContainer, AreaChart, CartesianGrid, XAxis, YAxis, Tooltip, Area } from 'recharts';

export default function ThroughputView() {

  const [hillaStat, setHillaStat] = useState<HillaStat | null>(null);
  useEffect(() => {

    ProAPIEndpoint.convert2Chart().then(hs => setHillaStat(hs || null));
  }, []);  

  if (!hillaStat) return null;

  return (
    <>
    <h5>Stats are available since {hillaStat.sinceDate}</h5>
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart
          width={500}
          height={400}
          data={hillaStat.chartPoints}
          margin={{
            top: 10,
            right: 30,
            left: 0,
            bottom: 0,
          }}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="strDate" />
          <YAxis />
          <Tooltip />
          <Area type="monotone" dataKey="routed" stackId="1" stroke="#89729E" fill="#89729E" />
          <Area type="monotone" dataKey="extracted" stackId="1" stroke="#89C4F4" fill="#89C4F4" />
          <Area type="monotone" dataKey="sent" stackId="1" stroke="#87D37C" fill="#87D37C" />
          <Area type="monotone" dataKey="loaded" stackId="1" stroke="#FFD700" fill="#FFD700" />
          <Area type="monotone" dataKey="unrouted" stackId="1" stroke="#f39c12" fill="#f39c12" />                
        </AreaChart>
      </ResponsiveContainer>    
    </>

  );
}

