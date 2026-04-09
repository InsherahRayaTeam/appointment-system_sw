package org.example.notification;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Objects;
import java.util.Properties;

/**
 * Represents email notification service in the system.
 *
 * <p>Run the app with JVM properties similar to:</p>
 * <pre>
 * -Dmail.smtp.host=smtp.gmail.com
 * -Dmail.smtp.port=587
 * -Dmail.smtp.auth=true
 * -Dmail.smtp.starttls.enable=true
 * -Dapp.mail.username=your_email@gmail.com
 * -Dapp.mail.password=your_16_char_app_password
 * -Dapp.mail.from=your_email@gmail.com
 * </pre>
 */
public class EmailNotificationService implements NotificationService {

    private static final String SMTP_HOST = "mail.smtp.host";
    private static final String SMTP_PORT = "mail.smtp.port";
    private static final String SMTP_AUTH = "mail.smtp.auth";
    private static final String SMTP_STARTTLS = "mail.smtp.starttls.enable";
    private static final String APP_MAIL_USERNAME = "app.mail.username";
    private static final String APP_MAIL_PASSWORD = "app.mail.password";
    private static final String APP_MAIL_FROM = "app.mail.from";

    /**
     * Creates a new email notification service object with the given values.
     */
    public EmailNotificationService() {
    }

    /**
     * Sends a notification to the given recipient.
     *
     * @param to recipient address
     * @param subject subject text to show or send
     * @param body message text to show or send
     */
    @Override
    public void send(String to, String subject, String body) {
        String recipient = requireText(to, "to");
        String messageSubject = requireText(subject, "subject");
        String messageBody = requireText(body, "body");

        String username = requireConfig(APP_MAIL_USERNAME);
        String password = requireConfig(APP_MAIL_PASSWORD);
        String from = requireConfig(APP_MAIL_FROM);

        Properties properties = new Properties();
        properties.put(SMTP_HOST, requireConfig(SMTP_HOST));
        properties.put(SMTP_PORT, requireConfig(SMTP_PORT));
        properties.put(SMTP_AUTH, requireConfig(SMTP_AUTH));
        properties.put(SMTP_STARTTLS, requireConfig(SMTP_STARTTLS));

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(messageSubject);
            message.setText(messageBody);
            Transport.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException(
                    "Failed to send appointment notification email to " + recipient + ".",
                    ex
            );
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    private String requireConfig(String key) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(key);
        }

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing SMTP configuration value for '" + key + "'."
            );
        }

        return Objects.requireNonNull(value).trim();
    }
}

