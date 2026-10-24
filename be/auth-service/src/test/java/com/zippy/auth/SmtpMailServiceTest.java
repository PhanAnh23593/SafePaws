package com.zippy.auth;

import com.zippy.auth.service.OtpService;
import com.zippy.auth.service.impl.SmtpMailService;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SmtpMailServiceTest {
    @ParameterizedTest
    @EnumSource(OtpService.Purpose.class)
    void sendsUtf8HtmlAndPlainTextForEachPurpose(OtpService.Purpose purpose) throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(sender.createMimeMessage()).thenReturn(message);

        new SmtpMailService(sender, "hello@example.com")
            .sendOtp("user@example.com", "012345", purpose);
        message.saveChanges();

        verify(sender).send(message);
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
        assertThat(message.getSubject()).contains(purpose == OtpService.Purpose.REGISTER
            ? "Xác minh tài khoản" : "Đặt lại mật khẩu");
        String html = body(message, "text/html");
        assertThat(html).contains("012345", "3 phút", "style=").doesNotContain("{{");
        assertThat(body(message, "text/plain")).contains("012345", "3 phút");
    }

    private String body(Part part, String mimeType) throws Exception {
        if (part.isMimeType(mimeType)) return (String) part.getContent();
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                String found = body(multipart.getBodyPart(i), mimeType);
                if (found != null) return found;
            }
        }
        return null;
    }
}
