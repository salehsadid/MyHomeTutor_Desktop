package com.myhometutor.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    // NOTE: For this to work with Gmail, you need to use an "App Password".
    // 1. Go to Google Account -> Security -> 2-Step Verification -> App passwords
    // 2. Generate a new app password and paste it below.
    private static final String SENDER_EMAIL = "myhometutor.manager@gmail.com"; 
    private static final String SENDER_PASSWORD = "tkzp tojf dmoj sbhc"; // REPLACE WITH YOUR APP PASSWORD

    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public static boolean sendEmail(String recipientEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email sent successfully to " + recipientEmail);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendOTP(String recipientEmail, String otp) {
        String subject = "MyHomeTutor - Email Verification OTP";
        String body = "<html><body style='font-family: Arial, sans-serif;'>" +
                "<p>Your OTP for MyHomeTutor registration is:</p>" +
                "<h1 style='color: #2196F3; font-size: 48px; font-weight: bold;'>" + otp + "</h1>" +
                "<p style='color: #f82727ff;'>Please do not share this code with anyone.</p>" +
                "</body></html>";
        return sendEmail(recipientEmail, subject, body);
    }
}
