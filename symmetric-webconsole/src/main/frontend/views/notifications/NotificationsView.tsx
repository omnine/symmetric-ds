/*
[SymmetricDS Notifications](https://medium.com/data-weekly/symmetricds-notifications-3a9044a4c0a)
The notification rows are tied to the monitors by the severity_level specified on the monitors. 
For example, if you set up a notification for a severity level of INFO (100), 
then it will send emails for all monitors that are configured with the severity level of 100 
or above. Similarly, notifications with a severity level of ‘SEVERE’ (300) will send emails 
for all monitors with a severity level of 300 or above.
*/
import { useEffect, useState } from 'react';
import { Button, Checkbox, Form, Input, Select, Space, message, Slider, Switch, Table, Drawer } from 'antd';
import type { SliderSingleProps } from 'antd';
import type { FormProps } from 'antd';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';
import HillaNotification from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/HillaNotification';
import '@vaadin/icons';
import { Icon } from '@vaadin/react-components/Icon.js';

const marks: SliderSingleProps['marks'] = {
  0: 'OK',
  100: 'INFO',
  200: 'WARNING',
  300: {
    style: {
      color: '#f50',
    },
    label: <strong>SEVERE</strong>,
  },
};

export default function NotificationsView() {
  const [form] = Form.useForm();
  const [notifications, setNotifications] = useState<(HillaNotification | undefined)[]>([]);

  const [typeOptions, setTypeOptions] = useState<string[]>([]);
  const [targetNodeOptions, setTargetNodeOptions] = useState<string[]>([]);

  useEffect(() => {
    ProAPIEndpoint.getNotifications().then((notifications: (HillaNotification | undefined)[] | undefined) => {
      if (notifications) {
        setNotifications(notifications);
      } else {
        message.error('Failed to load notifications');
      }
    });

    ProAPIEndpoint.getTargetNodes().then((targetNodes: (string | undefined)[] | undefined) => {
      if (targetNodes) {
        setTargetNodeOptions(targetNodes.filter((node): node is string => node !== undefined));
        form.setFieldsValue({ targetNode: targetNodes[0] });
      } else {
        message.error('Failed to load target nodes');
      }
    });

    ProAPIEndpoint.getNotificationTypes().then((notificationTypes: (string | undefined)[] | undefined) => {
      if (notificationTypes) {
        const filteredNotificationTypes = notificationTypes.filter((type): type is string => type !== undefined);
        setTypeOptions(filteredNotificationTypes);
        form.setFieldsValue({ type: filteredNotificationTypes[0] });
      } else {
        message.error('Failed to load notification types');
      }
    });

  }, []);

  const onFinish: FormProps<HillaNotification>['onFinish'] = (values) => {
    // console.log('Success:', values);

    ProAPIEndpoint.saveNotification(values);

    setOpen(false);
  };
  
  const onFinishFailed: FormProps<HillaNotification>['onFinishFailed'] = (errorInfo) => {
    // console.log('Failed:', errorInfo);
  };

  const columns = [
    {
      title: 'Notification Id',
      dataIndex: 'notificationId',
      key: 'notificationId',
    },
    {
      title: 'Node GroupId ID',
      dataIndex: 'nodeGroupId',
      key: 'nodeGroupId',
    },
    {
      title: 'External ID',
      dataIndex: 'externalId',
      key: 'externalId',
    },
    
    {
      title: 'Severity Level',
      dataIndex: 'severityLevel',
      key: 'severityLevel',
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: 'Expression',
      dataIndex: 'expression',
      key: 'expression',
    },
    {
      title: 'Enabled',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (_: any, record: HillaNotification) => {
        if (record.enabled) {
          return (<Icon icon="vaadin:play" />);
        }
        return <Icon icon="vaadin:stop" />;
      },      
    },    
  ];

  const [open, setOpen] = useState(false);
  const onClose = () => {
    setOpen(false);
  };

  return (
    <>
       <Space direction="vertical" size="middle" style={{ display: 'flex' }}>
        <Table<HillaNotification> dataSource={notifications.filter((notification): notification is HillaNotification => notification !== undefined)} columns={columns} />
          <Button type="primary" onClick={()=>setOpen(true)}>Create</Button>
          <div>
            A notification sends a message to the user when a monitor event records a system problem. 
            First configure a monitor to watch the system and record events with a specific severity level. 
            Then, configure a notification to match the severity level and write to the log or send an email.
          </div>
       </Space>


      <Drawer
        title="Create a new notification"
        placement="right"
        size="large"
        onClose={onClose}
        open={open}
        extra={
          <Space>
            <Button onClick={onClose}>Cancel</Button>
            <Button type="primary" onClick={onClose}>
              OK
            </Button>
          </Space>
        }
      >
      <Form form={form}
      name="basic"
      labelCol={{ span: 8 }}
      wrapperCol={{ span: 16 }}
      style={{ maxWidth: 600 }}
      initialValues={{ user_auth: true, transport: 'smtp', starttls: false, ssl_auth: false, allow_untrust_cert: true }}
      onFinish={onFinish}
      onFinishFailed={onFinishFailed}
      autoComplete="off"
      >
      <Form.Item<HillaNotification >
        label="Notification Id"
        name="notificationId"
        rules={[{ required: true, message: 'Please input smtp port!' }]}
      >
        <Input />
      </Form.Item>

      <Form.Item<HillaNotification >
        label="Target Nodes"
        name="nodeGroupId"
        rules={[{ required: true, message: 'Please select Transport!' }]}
      >
        <Select
          style={{ width: 120 }}
          options={targetNodeOptions.map((value) => ({ value, label: value }))}
        />
      </Form.Item>

      <Form.Item<HillaNotification >
        label="Notification Type"
        name="type"
        rules={[{ required: true, message: 'Please select Transport!' }]}
      >
        <Select
          style={{ width: 120 }}
          options={typeOptions.map((value) => ({ value, label: value }) ) }
          
        />
      </Form.Item>

      <Form.Item<HillaNotification >
          label="Email Address"
          name="expression"
          rules={[{ required: true, message: 'Please input the connection username!' }]}
        >
          <Input />
      </Form.Item> 
      <Form.Item<HillaNotification > name="severityLevel" label="Severity Level">
        <Slider defaultValue={0} max={300} marks={marks}/>
      </Form.Item>

      <Form.Item<HillaNotification > name="enabled"  label={null}>
        <Switch checkedChildren="Enabled" unCheckedChildren="Disabled" defaultChecked />
      </Form.Item>


      <Form.Item label={null}>
        <Space>
          <Button type="primary" htmlType="submit" >
            Save
          </Button>

        </Space>
      </Form.Item>
      </Form>
      </Drawer>
    </>

  );
}
