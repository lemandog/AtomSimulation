package org.lemandog.Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Messenger {
    public static void send(String recipient, String messageText, File attachments, String header) {
        String from = "atomsim@internet.ru";
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
                String userName = null;
                String password = null;
                try (BufferedReader br = new BufferedReader(new FileReader("emailCred.txt"))) {
                    userName = br.readLine();
                    password = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Please, put emailCred.txt near running jar with address and key on separate line - more info in INFO table");
                    try {
                        new File("emailCred.txt").createNewFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return new PasswordAuthentication(userName, password);
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
            message.setSubject(header+"- Results of your simulation are ready");
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
