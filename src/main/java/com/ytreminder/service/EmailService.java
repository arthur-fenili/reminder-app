package com.ytreminder.service;

import com.ytreminder.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String pixKey;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String from,
            @Value("${reminder.pix-key}") String pixKey) {
        this.mailSender = mailSender;
        this.from = from;
        this.pixKey = pixKey;
    }

    public boolean sendReminder(Member member) {
        try {
            String amount = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"))
                    .format(member.getAmount());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(member.getEmail());
            message.setSubject("TA NA HORA DO PIXXXX");
            message.setText("""
                    EAI XUPINGOLE

                    Passando pra lembrar do pagamento do Youtube, favor enviar o comprovante no grupo do zap.

                    Seu valor: %s
                    Meu pix (celular): %s

                    beijos
                    """.formatted(amount, pixKey));

            mailSender.send(message);
            log.info("E-mail enviado para {} ({})", member.getName(), member.getEmail());
            return true;
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {} ({}): {}", member.getName(), member.getEmail(), e.getMessage());
            return false;
        }
    }
}
