import { useEffect, useState } from 'react';
import { useForm } from 'antd/es/form/Form';
import type { FormProps } from 'antd';
import { Button, Checkbox, Form, Input, Select, Space } from 'antd';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';

import MailServerSetting from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/MailServerSetting';

export default function MailServerView() {
  const [form] = Form.useForm();
  const [submitType, setSubmitType] = useState<number>(0);


  
  const onFinish: FormProps<MailServerSetting>['onFinish'] = (values) => {
    console.log('Success:', values);
    if(submitType === 1) {
      setSubmitType(0);
      ProAPIEndpoint.testSMTPConnection(values);
    }
    else{
      ProAPIEndpoint.saveMailServerSetting(values);
    }
    
  };
  
  const onFinishFailed: FormProps<MailServerSetting>['onFinishFailed'] = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };
  


  return (
    <Form form={form}
      name="basic"
      labelCol={{ span: 8 }}
      wrapperCol={{ span: 16 }}
      style={{ maxWidth: 600 }}
      initialValues={{ user_auth: true }}
      onFinish={onFinish}
      onFinishFailed={onFinishFailed}
      autoComplete="off"
    >
      <Form.Item<MailServerSetting>
        label="From Address"
        name="from"
        rules={[{ required: true, message: 'Please input sender addr!' }]}
      >
        <Input />
      </Form.Item>      
      <Form.Item<MailServerSetting>
        label="Hostname"
        name="host"
        rules={[{ required: true, message: 'Please input smtp host!' }]}
      >
        <Input />
      </Form.Item>
      <Form.Item<MailServerSetting>
        label="Port"
        name="port"
        rules={[{ required: true, message: 'Please input smtp port!' }]}
      >
        <Input />
      </Form.Item>


      <Form.Item<MailServerSetting>
        label="Transport"
        name="transport"
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


      <Form.Item<MailServerSetting> name="starttls" valuePropName="checked" label={null}>
        <Checkbox>Use StartTLS</Checkbox>
      </Form.Item>      
      <Form.Item<MailServerSetting> name="ssl_auth" valuePropName="checked" label={null}>
        <Checkbox>Use SSL authentication</Checkbox>
      </Form.Item>
      <Form.Item<MailServerSetting> name="allow_untrust_cert" valuePropName="checked" label={null}>
        <Checkbox>Allow untrusted cert</Checkbox>
      </Form.Item>

      <Form.Item<MailServerSetting> name="user_auth" valuePropName="checked" label={null}>
        <Checkbox>Use authentication</Checkbox>
      </Form.Item>

      <Form.Item shouldUpdate={(prevValues, currentValues) => prevValues.user_auth !== currentValues.user_auth}>
        {({ getFieldValue }) => {
          const userAuth = getFieldValue('user_auth');
          return userAuth === true ? (
              <>
                <Form.Item<MailServerSetting>
                  label="Username"
                  name="username"
                  rules={[{ required: true, message: 'Please input the connection username!' }]}
                >
                  <Input />
                </Form.Item>
      
                <Form.Item<MailServerSetting>
                  label="Password"
                  name="password"
                  rules={[{ required: true, message: 'Please input the connection password!' }]}
                >
                  <Input.Password />
                </Form.Item>
              </>
          ) : null;
        }}
      </Form.Item>
 

      <Form.Item label={null}>
        <Space>
          <Button type="primary" htmlType="submit">
            Submit
          </Button>
          <Button onClick={() => {
            setSubmitType(1);
            form.submit();
          }}>
            Test Connection
          </Button>
          <Button>
            Send Test Email
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
}
