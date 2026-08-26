package robertoCafagna.BE_capstone.services;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;


    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email inviata a {} — oggetto: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Errore durante l'invio email a {}", to, e);

        }
    }

    public void sendVerificationEmail(String to, String username, String verificationLink) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                    <h2>Benvenuto %s!</h2>
                    <p>Grazie per esserti registrato su Rider App.</p>
                    <p>Conferma il tuo indirizzo email cliccando sul link sottostante:</p>
                    <p><a href="%s" style="background:#2a9d8f;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;">Verifica email</a></p>
                    <p>Se non hai creato tu questo account, ignora semplicemente questa email.</p>
                </div>
                """.formatted(username, verificationLink);
        sendHtmlEmail(to, "Conferma la tua email — Rider App", html);
    }
}
