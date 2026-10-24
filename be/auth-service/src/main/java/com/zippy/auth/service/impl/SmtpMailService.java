package com.zippy.auth.service.impl;

import com.zippy.auth.service.MailService;
import com.zippy.auth.service.OtpService;
import com.zippy.auth.exception.AppException;
import com.zippy.auth.exception.ErrorCode;
import org.springframework.mail.MailException;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class SmtpMailService implements MailService {
    private final JavaMailSender mailSender;
    private final String from;
    private final String otpTemplate;

    public SmtpMailService(JavaMailSender mailSender, @Value("${auth.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
        try {
            this.otpTemplate = new ClassPathResource("templates/mail/otp.html")
                .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load OTP email template", ex);
        }
    }

    @Override
    public void sendOtp(String email, String otp, OtpService.Purpose purpose) {
        if (from == null || from.isBlank()) throw new AppException(ErrorCode.AUTH_MAIL_UNAVAILABLE);
        boolean registration = purpose == OtpService.Purpose.REGISTER;
        String title = registration ? "Xác minh tài khoản" : "Đặt lại mật khẩu";
        String description = registration
            ? "Dùng mã bên dưới để hoàn tất đăng ký tài khoản SafePaws của bạn."
            : "Dùng mã bên dưới để xác minh yêu cầu đặt lại mật khẩu SafePaws của bạn.";
        String plainText = title + "\n\n" + description + "\n\nMã xác minh: " + otp
            + "\nMã có hiệu lực trong 3 phút. Không chia sẻ mã này với bất kỳ ai."
            + "\nNếu bạn không thực hiện yêu cầu này, hãy bỏ qua email.\n\nSafePaws";
        String html = otpTemplate.replace("{{title}}", HtmlUtils.htmlEscape(title))
            .replace("{{description}}", HtmlUtils.htmlEscape(description))
            .replace("{{otp}}", HtmlUtils.htmlEscape(otp));
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("SafePaws – " + title);
            helper.setText(plainText, html);
            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            throw new AppException(ErrorCode.AUTH_MAIL_UNAVAILABLE);
        }
    }
}
