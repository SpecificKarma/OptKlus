package com.gmail.specifikarma.optimumklus.networking;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {

//    PRECONDITION
//    Step 1 : Turn On less secure
//    https://myaccount.google.com/lesssecureapps
//    Step 2 :  Allow Display Unlock Captcha
//    https://accounts.google.com/b/0/DisplayUnlockCaptcha
    /**CHANGE ACCORDINGLY**/
    private static final String SMTP_HOST_NAME = "smtp.gmail.com"; //can be your host server smtp@yourdomain.com
    private static final String SMTP_AUTH_USER = "optklus.clients@gmail.com"; //your login username/email
    private static final String SMTP_AUTH_PWD  = "optklus.clients2020"; //password/secret

    private static Message message;

    public static void sendEmail(String to, String subject, String msg, ArrayList<File> links){

        ArrayList<File> files = links;

        String from = "optklus.clients@gmail.com"; //from

        final String username = SMTP_AUTH_USER;
        final String password = SMTP_AUTH_PWD;

        // Assuming you are sending email through relay.jangosmtp.net
        String host = SMTP_HOST_NAME;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create a default MimeMessage object.
            message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject(subject);

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            messageBodyPart.setContent(msg, "text/html");

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

//          // Part two is attachment
            for (File f : files) {
                messageBodyPart = new MimeBodyPart();
                DataSource fds = new FileDataSource( new File(f.getPath()));
                messageBodyPart.setDataHandler(new DataHandler(fds));
                messageBodyPart.setHeader("Content-ID", "<image>");
                multipart.addBodyPart(messageBodyPart);
            }

            // Send the complete message parts
            message.setContent(multipart);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try  {
                        // Send message
                        Transport.send(message);
                        Log.i("Mail", "Sent message successfully....");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Mail", "ERROR message");
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}