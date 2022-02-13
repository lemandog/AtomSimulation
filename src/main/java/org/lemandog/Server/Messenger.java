package org.lemandog.Server;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Messenger {
    public static void send(String recipient, String messageText, File attachments) {
        String from = "atomsim@internet.ru";
        //smtp
        String host = "smtp.mail.ru";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication("atomsim@internet.ru", "hZmWWfD7uLv8E3GPMrPb");

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            // Set Subject: header field
            message.setSubject("Results of your simulation with timestamp: " + LocalDateTime.now() +  " - READY");
            // Now set the actual message
            MimeMultipart content = new MimeMultipart();
            MimeBodyPart attachmentPart = new MimeBodyPart();
            BodyPart textPart = new MimeBodyPart();
            textPart.setText(messageText);

            attachmentPart.setText(messageText);
            attachmentPart.attachFile(attachments);
            content.addBodyPart(attachmentPart);
            content.addBodyPart(textPart);
            message.setContent(content);

            System.out.println("sending...");
            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException | IOException mex) {
            mex.printStackTrace();
        }
    }
}
