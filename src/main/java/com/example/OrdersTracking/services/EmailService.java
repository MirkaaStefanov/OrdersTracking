package com.example.OrdersTracking.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // MULTIPART_MODE_MIXED_RELATED is strictly required for inline images
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("noreply@borovooko.com"); // Make sure this matches your brand

            Context context = new Context();
            context.setVariables(variables);

            // 1. PROCESS HTML FIRST
            String htmlContent = templateEngine.process(templateName, context);

            // 2. SET TEXT SECOND (This initializes the multipart structure)
            helper.setText(htmlContent, true);

            // 3. ADD INLINE IMAGE THIRD
            // The first argument "logo.png" must match the th:src="|cid:logo.png|" in HTML
            // The path depends on where your file is. Usually: src/main/resources/static/images/logo.png
            helper.addInline("logo.png", new ClassPathResource("static/images/logo.png"));

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
