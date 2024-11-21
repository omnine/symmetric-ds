package com.jumpmind.symmetric.console.service.impl;

import com.jumpmind.symmetric.console.service.IMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.properties.TypedProperties;
import org.jumpmind.security.ISecurityService;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.db.ISymmetricDialect;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.service.IParameterService;
import org.jumpmind.symmetric.service.impl.AbstractService;

public class MailService extends AbstractService implements IMailService, ISymmetricEngineAware {
   protected ISecurityService securityService;
   protected static final String JAVAMAIL_HOST_NAME = "mail.host";
   protected static final String JAVAMAIL_TRANSPORT = "mail.transport";
   protected static final String JAVAMAIL_PORT_NUMBER = "mail.smtp.port";
   protected static final String JAVAMAIL_PORT_NUMBER_SSL = "mail.smtps.port";
   protected static final String JAVAMAIL_FROM = "mail.from";
   protected static final String JAVAMAIL_USE_STARTTLS = "mail.smtp.starttls.enable";
   protected static final String JAVAMAIL_USE_AUTH = "mail.smtp.auth";
   protected static final String JAVAMAIL_TRUST_HOST = "mail.smtp.ssl.trust";
   protected static final String JAVAMAIL_TRUST_HOST_SSL = "mail.smtps.ssl.trust";
   protected static final String JAVAMAIL_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
   protected static final String JAVAMAIL_SSL_PROTOCOLS = "mail.smtp.ssl.protocols";

   public MailService() {
   }

   public MailService(IParameterService parameterService, ISymmetricDialect symmetricDialect) {
      super(parameterService, symmetricDialect);
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.symmetricDialect = engine.getSymmetricDialect();
      this.parameterService = engine.getParameterService();
      this.securityService = engine.getSecurityService();
      this.tablePrefix = this.parameterService.getTablePrefix();
      this.platform = this.symmetricDialect.getPlatform();
      this.sqlTemplate = this.symmetricDialect.getPlatform().getSqlTemplate();
      this.sqlTemplateDirty = this.symmetricDialect.getPlatform().getSqlTemplateDirty();
   }

   @Override
   public String sendEmail(String subject, String text, String toRecipients) {
      return this.sendEmail(subject, text, toRecipients, null, null);
   }

   public String sendEmail(String subject, String text, String toRecipients, String ccRecipients, String bccRecipients) {
      String password = this.decryptPassword(this.parameterService.getString("smtp.password"));
      return this.sendEmail(
         subject,
         text,
         toRecipients,
         ccRecipients,
         bccRecipients,
         this.getJavaMailProperties(),
         this.parameterService.getString("smtp.transport", "smtp"),
         this.parameterService.is("smtp.auth", false),
         this.parameterService.getString("smtp.user"),
         password
      );
   }

   @Override
   public String sendEmail(String subject, String text, String toRecipients, TypedProperties prop) {
      return this.sendEmail(subject, text, toRecipients, null, null, prop);
   }

   public String sendEmail(String subject, String text, String toRecipients, String ccRecipients, String bccRecipients, TypedProperties prop) {
      return this.sendEmail(
         subject,
         text,
         toRecipients,
         ccRecipients,
         bccRecipients,
         this.getJavaMailProperties(prop),
         prop.get("smtp.transport", "smtp"),
         prop.is("smtp.auth", false),
         prop.get("smtp.user"),
         this.decryptPassword(prop.get("smtp.password"))
      );
   }

   protected String sendEmail(
      String subject,
      String text,
      String toRecipients,
      String ccRecipients,
      String bccRecipients,
      Properties prop,
      String transportType,
      boolean useAuth,
      String user,
      String password
   ) {
      Session session = Session.getInstance(prop);
      ByteArrayOutputStream ba = null;
      if (this.log.isDebugEnabled()) {
         session.setDebug(true);
         ba = new ByteArrayOutputStream();
         PrintStream ps = new PrintStream(ba);
         session.setDebugOut(ps);
      }

      Transport transport;
      try {
         transport = session.getTransport(transportType);
      } catch (NoSuchProviderException var31) {
         this.log.error("Failure while obtaining transport", var31);
         return this.getNestedErrorMessage(var31);
      }

      try {
         if (useAuth) {
            transport.connect(user, password);
         } else {
            transport.connect();
         }
      } catch (MessagingException var30) {
         this.log.error("Failure while connecting to transport", var30);
         return this.getNestedErrorMessage(var30);
      }

      label127: {
         String var16;
         try {
            MimeMessage message = new MimeMessage(session);
            message.setSentDate(new Date());
            if (StringUtils.isNotEmpty(toRecipients)) {
               message.setRecipients(RecipientType.TO, toRecipients);
            }

            if (StringUtils.isNotEmpty(ccRecipients)) {
               message.setRecipients(RecipientType.CC, ccRecipients);
            }

            if (StringUtils.isNotEmpty(bccRecipients)) {
               message.setRecipients(RecipientType.BCC, bccRecipients);
            }

            message.setSubject(subject);
            message.setText(text);
            message.setFrom(new InternetAddress(prop.getProperty("mail.from")));

            try {
               transport.sendMessage(message, message.getAllRecipients());
               break label127;
            } catch (MessagingException var32) {
               this.log.error("Failure while sending notification", var32);
               var16 = this.getNestedErrorMessage(var32);
            }
         } catch (MessagingException var33) {
            this.log.error("Failure while preparing notification", var33);
            return var33.getMessage();
         } finally {
            try {
               transport.close();
            } catch (MessagingException var29) {
            }
         }

         return var16;
      }

      if (this.log.isDebugEnabled()) {
         this.log.debug(ba.toString());
      }

      return null;
   }

   @Override
   public String testTransport(TypedProperties prop) {
      String error = null;
      Transport transport = null;

      try {
         this.log.debug("SMTP parameters: {}", prop);
         Properties javaMailProps = this.getJavaMailProperties(prop);
         this.log.debug("JavaMail properties: {}", javaMailProps);
         Session session = Session.getInstance(javaMailProps);
         transport = session.getTransport(prop.get("smtp.transport", "smtp"));
         if (prop.is("smtp.auth", false)) {
            transport.connect(prop.get("smtp.user"), this.decryptPassword(prop.get("smtp.password")));
         } else {
            transport.connect();
         }
      } catch (NoSuchProviderException var16) {
         error = this.getNestedErrorMessage(var16);
         this.log.warn("Test of mail service failed:", var16);
      } catch (MessagingException var17) {
         error = this.getNestedErrorMessage(var17);
         this.log.warn("Test of mail service failed:", var17);
      } finally {
         try {
            if (transport != null) {
               transport.close();
            }
         } catch (MessagingException var15) {
         }
      }

      return error;
   }

   protected String getNestedErrorMessage(Exception e) {
      String error = e.getMessage();
      Throwable e2 = e.getCause();
      if (e2 != null) {
         error = error + "\n" + e2.getMessage();
         Throwable e3 = e2.getCause();
         if (e3 != null) {
            error = error + "\n" + e3.getMessage();
         }
      }

      return error;
   }

   protected Properties getJavaMailProperties() {
      Properties prop = new Properties();
      prop.setProperty("mail.host", this.parameterService.getString("smtp.host", "localhost"));
      prop.setProperty("mail.smtp.port", this.parameterService.getString("smtp.port", "25"));
      prop.setProperty("mail.smtps.port", this.parameterService.getString("smtp.port", "25"));
      prop.setProperty("mail.from", this.parameterService.getString("smtp.from", "root@localhost"));
      prop.setProperty("mail.smtp.starttls.enable", this.parameterService.getString("smtp.starttls", "false"));
      prop.setProperty("mail.smtp.auth", this.parameterService.getString("smtp.auth", "false"));
      prop.setProperty("mail.smtp.ssl.trust", this.parameterService.is("smtp.allow.untrusted.cert", false) ? "*" : "");
      prop.setProperty("mail.smtps.ssl.trust", this.parameterService.is("smtp.allow.untrusted.cert", false) ? "*" : "");
      prop.setProperty("mail.smtp.auth", this.parameterService.getString("smtp.auth", "false"));
      if (StringUtils.isNotBlank(this.parameterService.getString("smtp.socket.factory.class"))) {
         prop.setProperty("mail.smtp.socketFactory.class", this.parameterService.getString("smtp.socket.factory.class"));
      }

      if (StringUtils.isNotBlank(this.parameterService.getString("smtp.ssl.protocols"))) {
         prop.setProperty("mail.smtp.ssl.protocols", this.parameterService.getString("smtp.ssl.protocols"));
      }

      return prop;
   }

   protected Properties getJavaMailProperties(TypedProperties typedProp) {
      Properties prop = new Properties();
      prop.setProperty("mail.host", typedProp.get("smtp.host", "localhost"));
      prop.setProperty("mail.smtp.port", typedProp.get("smtp.port", "25"));
      prop.setProperty("mail.smtps.port", typedProp.get("smtp.port", "25"));
      prop.setProperty("mail.from", typedProp.get("smtp.from", "root@localhost"));
      prop.setProperty("mail.smtp.starttls.enable", String.valueOf(typedProp.is("smtp.starttls", false)));
      prop.setProperty("mail.smtp.auth", String.valueOf(typedProp.is("smtp.auth", false)));
      prop.setProperty("mail.smtp.ssl.trust", typedProp.is("smtp.allow.untrusted.cert", false) ? "*" : "");
      prop.setProperty("mail.smtps.ssl.trust", typedProp.is("smtp.allow.untrusted.cert", false) ? "*" : "");
      if (StringUtils.isNotBlank(typedProp.get("smtp.socket.factory.class"))) {
         prop.setProperty("mail.smtp.socketFactory.class", typedProp.get("smtp.socket.factory.class"));
      }

      if (StringUtils.isNotBlank(typedProp.get("smtp.ssl.protocols"))) {
         prop.setProperty("mail.smtp.ssl.protocols", typedProp.get("smtp.socket.factory.class"));
      }

      return prop;
   }

   protected String decryptPassword(String password) {
      if (password != null && password.startsWith("enc:")) {
         try {
            return this.securityService.decrypt(password.substring("enc:".length()));
         } catch (Exception var3) {
            throw new IllegalStateException(
               "Failed to decrypt the mail server password.  Please re-enter the password on the Configure -> Mail Server screen", var3
            );
         }
      } else {
         return password;
      }
   }
}
