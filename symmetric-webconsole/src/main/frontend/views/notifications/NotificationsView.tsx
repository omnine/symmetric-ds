import { useEffect, useState } from 'react';
import { Button, Checkbox, Form, Input, Select, Space, message, Slider, Switch } from 'antd';
import type { SliderSingleProps } from 'antd';
import type { FormProps } from 'antd';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';

type FieldType = {
  notificationId?: string;
  targetNode?: string;
  type?: string;
  severityLevel?: number;
  enabled?: boolean;
  email?: string;
};

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

  const [typeOptions, setTypeOptions] = useState<string[]>([]);
  const [targetNodeOptions, setTargetNodeOptions] = useState<string[]>([]);

  useEffect(() => {
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

  const onFinish: FormProps<FieldType>['onFinish'] = (values) => {
    console.log('Success:', values);
  };
  
  const onFinishFailed: FormProps<FieldType>['onFinishFailed'] = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  return (
    <>

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
      <Form.Item<FieldType >
        label="Notification Id"
        name="notificationId"
        rules={[{ required: true, message: 'Please input smtp port!' }]}
      >
        <Input />
      </Form.Item>

      <Form.Item<FieldType >
        label="Target Nodes"
        name="targetNode"
        rules={[{ required: true, message: 'Please select Transport!' }]}
      >
        <Select
          style={{ width: 120 }}
          options={targetNodeOptions.map((value) => ({ value, label: value }))}
        />
      </Form.Item>

      <Form.Item<FieldType >
        label="Notification Type"
        name="type"
        rules={[{ required: true, message: 'Please select Transport!' }]}
      >
        <Select
          style={{ width: 120 }}
          options={typeOptions.map((value) => ({ value, label: value }) ) }
          
        />
      </Form.Item>

      <Form.Item<FieldType >
          label="Email Address"
          name="email"
          rules={[{ required: true, message: 'Please input the connection username!' }]}
        >
          <Input />
      </Form.Item> 
      <Form.Item<FieldType > name="severityLevel" label="Severity Level">
        <Slider defaultValue={0} max={300} marks={marks}/>
      </Form.Item>

      <Form.Item<FieldType > name="enabled"  label={null}>
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
    </>

  );
}
