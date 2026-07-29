package robertoCafagna.BE_capstone.services.AUTH;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;
import robertoCafagna.BE_capstone.services.MailService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final int TOKEN_VALIDITY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;


    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES));
            userRepository.save(user);

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String html = buildResetEmailHtml(user.getUsername(), resetLink);
            mailService.sendHtmlEmail(user.getEmail(), "Reimposta la tua password — Rider App", html);

            log.info("Richiesta reset password per l'utente {}", user.getId());
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new BadRequestException("Token non valido"));

        if (user.getResetPasswordTokenExpiry() == null
                || user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Il token è scaduto, richiedine uno nuovo");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reimpostata per l'utente {}", user.getId());
    }

    private String buildResetEmailHtml(String username, String resetLink) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                    <h2>Ciao %s,</h2>
                    <p>Hai richiesto di reimpostare la password del tuo account Rider App.</p>
                    <p>Clicca sul link sottostante per scegliere una nuova password. Il link è valido per %d minuti.</p>
                    <p><a href="%s" style="background:#e63946;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;">Reimposta password</a></p>
                    <p>Se non hai richiesto tu questa operazione, ignora semplicemente questa email.</p>
                </div>
                """.formatted(username, TOKEN_VALIDITY_MINUTES, resetLink);
    }
}
