import HillaStat from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HillaStat';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints';
import { useEffect, useState } from 'react';
import { ResponsiveContainer, AreaChart, CartesianGrid, XAxis, YAxis, Tooltip, Area } from 'recharts';

import { Flex, Input, Select } from 'antd';
import { useLingui } from "@lingui/react"

export default function ThroughputView() {
  const { i18n } = useLingui();

  const [period, setPeriod] = useState(1);
  const [isDay, setIsDay] = useState(false);
  const [hillaStat, setHillaStat] = useState<HillaStat | null>(null);

  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    const numberValue = parseInt(value, 10); // Convert string to number
    setPeriod(numberValue);
  };

  const handleChange = (value: string) => {
    // console.log(`selected ${value}`);
    if (value === 'day') {
      setIsDay(true);
    } else {
      setIsDay(false);
    }
  };


  const showLocalTime = (value: string) => {
    // console.log(`selected ${value}`);
    const d = new Date(value);
    return i18n.date(d, { dateStyle: "medium", timeStyle: "medium" });
  };  

  useEffect(() => {

    ProAPIEndpoint.convert2Chart(period, isDay).then((hs: HillaStat | undefined) => setHillaStat(hs || null));
  }, [period, isDay]);  

  if (!hillaStat) return null;

  return (
    <>
      <h5>Stats are available since {hillaStat.sinceDate ? showLocalTime(hillaStat.sinceDate) : 'N/A'}</h5>
      <Flex gap="middle">
        <Input defaultValue="1" onChange={onChange}/>
        <Select
        defaultValue="hour"
        style={{ width: 120 }}
        onChange={handleChange}
        options={[
          { value: 'hour', label: 'Hour' },
          { value: 'day', label: 'Day' },
        ]}
      />        
      </Flex>
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

