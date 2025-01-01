import { useEffect, useState } from 'react';
import { useForm } from 'antd/es/form/Form';
import type { FormProps } from 'antd';
import { Button, Checkbox, Form, Input, Select, Space, message, Drawer } from 'antd';
import { ProAPIEndpoint } from 'Frontend/generated/endpoints.js';

import MailServerSetting from 'Frontend/generated/com/jumpmind/symmetric/console/ui/data/MailServerSetting';

export default function MailServerView() {
  const [form] = Form.useForm();
  const [submitType, setSubmitType] = useState<number>(0);
  const [messageApi, contextHolder] = message.useMessage();
  const [recipientVisible, setRecipientVisible] = useState(false);


  useEffect(() => {
    ProAPIEndpoint.getMailServerSetting().then((mailServerSetting: MailServerSetting | undefined) => {
      if (mailServerSetting) {
        form.setFieldsValue(mailServerSetting);
      } else {
        messageApi.error('Failed to load mail server settings');
      }
    });
  }, []);

  
  const onFinish: FormProps<MailServerSetting>['onFinish'] = (values) => {
    // console.log('Success:', values);
    if(submitType === 1) {
      setSubmitType(0);
      ProAPIEndpoint.testSMTPConnection(values).then((result: string | undefined) => {
          if (result) {
              messageApi.info(result);
          } else {
              messageApi.error('Test SMTP connection failed');
          }
      });
    }
    else if(submitType === 2) {
      setSubmitType(0);
      ProAPIEndpoint.sendTestEmail(values).then((result: string | undefined) => {
          if (result) {
              messageApi.info(result);
          } else {
              messageApi.error('Send test email failed');
          }
      });
    }
    else{
      ProAPIEndpoint.saveMailServerSetting(values).then((result:number) => {
        if(result === 1){
          messageApi.error('Saved the Mail server setting successfully');
        }
        else{
          messageApi.warning('failed to save mail server setting');
        }

      });
    }
    
  };
  
  const onFinishFailed: FormProps<MailServerSetting>['onFinishFailed'] = (errorInfo) => {
    // console.log('Failed:', errorInfo);
  };
  
  const doSend = () => {
    setSubmitType(2);
    form.submit();
  }


  return (
    <>
      {contextHolder}


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

      {recipientVisible && (<Form.Item<MailServerSetting>
        label="Recipients"
        name="recipients"
        rules={[{ message: 'Please add some recipients, separated by semi-colon!' }]}
      >
        <Input />
      </Form.Item> )}

      <Form.Item label={null}>
        <Space>
          <Button type="primary" htmlType="submit" disabled={recipientVisible}>
            Save
          </Button>
          <Button disabled={recipientVisible} onClick={() => {
            setSubmitType(1);
            form.submit();
          }}>
            Test Connection
          </Button>
          <Button onClick={() => {
            if(recipientVisible){
              setSubmitType(2);
              form.submit();
            }
            setRecipientVisible(recipientVisible?false:true);
          }}>
            {recipientVisible?"Go":"Send Test Email"}
          </Button>
        </Space>
      </Form.Item>
    </Form>  
    </>

  );
}
