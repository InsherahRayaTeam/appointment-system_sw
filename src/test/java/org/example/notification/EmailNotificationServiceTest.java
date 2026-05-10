package org.example.notification;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class EmailNotificationServiceTest {

    private static final String SMTP_HOST = "mail.smtp.host";
    private static final String SMTP_PORT = "mail.smtp.port";
    private static final String SMTP_AUTH = "mail.smtp.auth";
    private static final String SMTP_STARTTLS = "mail.smtp.starttls.enable";
    private static final String APP_MAIL_USERNAME = "app.mail.username";
    private static final String APP_MAIL_PASSWORD = "app.mail.password";
    private static final String APP_MAIL_FROM = "app.mail.from";

    private final String[] configKeys = {
            SMTP_HOST,
            SMTP_PORT,
            SMTP_AUTH,
            SMTP_STARTTLS,
            APP_MAIL_USERNAME,
            APP_MAIL_PASSWORD,
            APP_MAIL_FROM
    };

    private Map<String, String> originalProperties;

    @BeforeEach
    void setUp() {
        originalProperties = new HashMap<>();
        for (String key : configKeys) {
            originalProperties.put(key, System.getProperty(key));
        }
    }

    @AfterEach
    void tearDown() {
        for (String key : configKeys) {
            String original = originalProperties.get(key);
            if (original == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, original);
            }
        }
    }

    @Test
    void constructor_CreatesServiceInstance() {
        EmailNotificationService service = new EmailNotificationService();
        assertNotNull(service);
    }

    @Test
    void send_WithValidInputs_SendsEmailThroughTransport() throws Exception {
        setRequiredConfig();
        EmailNotificationService service = new EmailNotificationService();
        AtomicReference<MimeMessage> capturedMessage = new AtomicReference<>();

        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> {
                capturedMessage.set((MimeMessage) invocation.getArgument(0));
                return null;
            });

            service.send("to@example.com", "Hello", "Message body");

            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }

        MimeMessage sentMessage = capturedMessage.get();
        assertNotNull(sentMessage);
        assertEquals("Hello", sentMessage.getSubject());
        assertEquals("Message body", sentMessage.getContent().toString().trim());
        assertEquals("to@example.com", sentMessage.getRecipients(Message.RecipientType.TO)[0].toString());
    }

    @Test
    void send_WithTrimmedInputs_UsesTrimmedValues() throws Exception {
        setRequiredConfig();
        EmailNotificationService service = new EmailNotificationService();
        AtomicReference<MimeMessage> capturedMessage = new AtomicReference<>();

        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> {
                capturedMessage.set((MimeMessage) invocation.getArgument(0));
                return null;
            });

            service.send("  to@example.com  ", "  Hello  ", "  Message body  ");

            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }

        MimeMessage sentMessage = capturedMessage.get();
        assertNotNull(sentMessage);
        assertEquals("Hello", sentMessage.getSubject());
        assertEquals("Message body", sentMessage.getContent().toString().trim());
        assertEquals("to@example.com", sentMessage.getRecipients(Message.RecipientType.TO)[0].toString());
    }

    @Test
    void send_WithNullRecipient_ThrowsIllegalArgumentExceptionAndDoesNotSend() {
        setRequiredConfig();
        EmailNotificationService service = new EmailNotificationService();

        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.send(null, "Hello", "Message")
            );

            assertTrue(ex.getMessage().contains("to cannot be blank"));
            transportMock.verify(() -> Transport.send(any(Message.class)), never());
        }
    }

    @Test
    void send_WithBlankSubject_ThrowsIllegalArgumentExceptionAndDoesNotSend() {
        setRequiredConfig();
        EmailNotificationService service = new EmailNotificationService();

        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.send("to@example.com", "   ", "Message")
            );

            assertTrue(ex.getMessage().contains("subject cannot be blank"));
            transportMock.verify(() -> Transport.send(any(Message.class)), never());
        }
    }

    @Test
    void send_WithEmptyBody_ThrowsIllegalArgumentExceptionAndDoesNotSend() {
        setRequiredConfig();
        EmailNotificationService service = new EmailNotificationService();

        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.send("to@example.com", "Hello", "")
            );

            assertTrue(ex.getMessage().contains("body cannot be blank"));
            transportMock.verify(() -> Transport.send(any(Message.class)), never());
        }
    }

    @Test
    void send_WithMissingSmtpConfig_ThrowsIllegalStateExceptionAndDoesNotSend() {
        setRequiredConfig();
        System.clearProperty(SMTP_HOST);
        EmailNotificationService service = new EmailNotificationService();

        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.send("to@example.com", "Hello", "Message")
            );

            assertTrue(ex.getMessage().contains("Missing SMTP configuration value for"));
            transportMock.verify(() -> Transport.send(any(Message.class)), never());
        }
    }

    @Test
    void send_WithInvalidEmailFormat_ThrowsIllegalStateExceptionAndDoesNotSend() {
        setRequiredConfig();
        EmailNotificationService service = new EmailNotificationService();

        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.send("not-an-email@@", "Hello", "Message")
            );

            assertTrue(ex.getMessage().contains("Failed to send appointment notification email"));
            assertInstanceOf(MessagingException.class, ex.getCause());
            transportMock.verify(() -> Transport.send(any(Message.class)), never());
        }
    }

    @Test
    void send_WhenTransportThrowsMessagingException_WrapsException() {
        setRequiredConfig();
        EmailNotificationService service = new EmailNotificationService();

        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            transportMock.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("smtp failed"));

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.send("to@example.com", "Hello", "Message")
            );

            assertTrue(ex.getMessage().contains("Failed to send appointment notification email."));
            assertInstanceOf(MessagingException.class, ex.getCause());
            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    private void setRequiredConfig() {
        System.setProperty(SMTP_HOST, "smtp.example.com");
        System.setProperty(SMTP_PORT, "587");
        System.setProperty(SMTP_AUTH, "true");
        System.setProperty(SMTP_STARTTLS, "true");
        System.setProperty(APP_MAIL_USERNAME, "sender@example.com");
        System.setProperty(APP_MAIL_PASSWORD, "secret");
        System.setProperty(APP_MAIL_FROM, "sender@example.com");
    }
}

