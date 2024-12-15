import { Button, Checkbox, Form, Input, Select, Space, message, Slider, Switch } from 'antd';
import type { SliderSingleProps } from 'antd';
import type { FormProps } from 'antd';
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
          defaultValue="smtp"
          style={{ width: 120 }}
          options={[
            { value: 'smtp', label: 'smtp' },
            { value: 'smtps', label: 'smtps' },
          ]}
        />
      </Form.Item>


      <Form.Item<FieldType >
        label="Notification Type"
        name="type"
        rules={[{ required: true, message: 'Please select Transport!' }]}
      >
        <Select
          defaultValue="smtp"
          style={{ width: 120 }}
          options={[
            { value: 'smtp', label: 'smtp' },
            { value: 'smtps', label: 'smtps' },
          ]}
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
