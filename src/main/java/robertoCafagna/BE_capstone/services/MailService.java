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

    public void sendCatalogSuggestionEmail(String adminAddress, String reporterUsername, String reporterEmail,
                                           String brandName, String modelName, Integer engineCc, String category,
                                           Integer yearStart, Integer yearEnd, Integer horsePower, Integer weightKg,
                                           String note) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                    <h2>Segnalazione modello mancante</h2>
                    <p><strong>Da:</strong> %s (%s)</p>
                    <hr>
                    <p><strong>Brand:</strong> %s</p>
                    <p><strong>Modello:</strong> %s</p>
                    <p><strong>Cilindrata:</strong> %s</p>
                    <p><strong>Categoria:</strong> %s</p>
                    <p><strong>Anni:</strong> %s</p>
                    <p><strong>Potenza:</strong> %s</p>
                    <p><strong>Peso:</strong> %s</p>
                    <hr>
                    <p><strong>Note aggiuntive:</strong><br>%s</p>
                </div>
                """.formatted(
                reporterUsername, reporterEmail,
                brandName,
                modelName != null ? modelName : "—",
                engineCc != null ? engineCc + " cc" : "—",
                category != null ? category : "—",
                formatYearRange(yearStart, yearEnd),
                horsePower != null ? horsePower + " CV" : "—",
                weightKg != null ? weightKg + " kg" : "—",
                note != null && !note.isBlank() ? note : "—"
        );
        sendHtmlEmail(adminAddress, "FlowRides — Nuova segnalazione catalogo", html);
    }

    private String formatYearRange(Integer yearStart, Integer yearEnd) {
        if (yearStart == null && yearEnd == null) return "—";
        if (yearStart != null && yearEnd != null) return yearStart + " – " + yearEnd;
        if (yearStart != null) return "Dal " + yearStart;
        return "Fino al " + yearEnd;

    }


    public void sendAccountDeletedByAdminEmail(String to, String username, String reason) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                    <h2>Il tuo account è stato eliminato</h2>
                    <p>Ciao %s,</p>
                    <p>Il tuo account su FlowRides è stato eliminato da un amministratore per il seguente motivo:</p>
                    <p style="background:#f4f4f4;padding:12px;border-radius:6px;">%s</p>
                    <p>I tuoi dati privati sono stati cancellati. I contenuti pubblici che avevi condiviso restano visibili ad altri utenti, ma senza più alcun collegamento alla tua identità.</p>
                    <p>Se ritieni che questa decisione sia un errore, contattaci rispondendo a questa email.</p>
                </div>
                """.formatted(username, reason);
        sendHtmlEmail(to, "Il tuo account FlowRides è stato eliminato", html);
    }


    public void sendAccountDeactivatedByAdminEmail(String to, String username, String reason) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                    <h2>Il tuo account è stato disattivato</h2>
                    <p>Ciao %s,</p>
                    <p>Il tuo account su FlowRides è stato disattivato da un amministratore.</p>
                    <p><strong>Motivo:</strong></p>
                    <p style="background:#f4f4f4;padding:12px;border-radius:6px;">%s</p>
                    <p>Non potrai accedere finché l'account non verrà riattivato. Se ritieni che questa decisione sia un errore, contattaci rispondendo a questa email.</p>
                </div>
                """.formatted(username, reason != null && !reason.isBlank() ? reason : "Nessun motivo specificato");
        sendHtmlEmail(to, "Il tuo account FlowRides è stato disattivato", html);
    }


    public void sendAccountReactivatedByAdminEmail(String to, String username) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                    <h2>Il tuo account è stato riattivato</h2>
                    <p>Ciao %s,</p>
                    <p>Buone notizie: il tuo account su FlowRides è stato riattivato da un amministratore. Puoi accedere di nuovo con le tue credenziali.</p>
                </div>
                """.formatted(username);
        sendHtmlEmail(to, "Il tuo account FlowRides è stato riattivato", html);
    }
}
