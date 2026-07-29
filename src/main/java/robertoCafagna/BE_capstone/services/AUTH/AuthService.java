package robertoCafagna.BE_capstone.services.AUTH;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.ADMIN.AuthResponseDTO;
import robertoCafagna.BE_capstone.DTO.AUTH.LoginRequestDTO;
import robertoCafagna.BE_capstone.DTO.AUTH.RegisterRequestDTO;
import robertoCafagna.BE_capstone.config.DefaultImageConfig;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;
import robertoCafagna.BE_capstone.security.JWTTools;
import robertoCafagna.BE_capstone.services.MailService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTTools jwtTools;
    private final AuthenticationManager authenticationManager;
    private final DefaultImageConfig defaultImageConfig;
    private final MailService mailService;


    @Value("${app.frontend-url}")
    private String frontendUrl;

    public String register(RegisterRequestDTO body) {
        if (userRepository.existsByUsername(body.username())) {
            throw new BadRequestException("Username già in uso!");
        }
        if (userRepository.existsByEmail(body.email())) {
            throw new BadRequestException("Email già in uso!");
        }

        User user = new User(body.username(), body.email(),
                passwordEncoder.encode(body.password())
        );

        user.setProfilePicture(
                defaultImageConfig.getDefaultAvatar()
        );


        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
        mailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationLink);

        return "Utente registrato con successo, controlla la tua email per verificare l'account";
    }

    public AuthResponseDTO login(LoginRequestDTO body) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.username(), body.password())
        );

        User user = (User) authentication.getPrincipal();
        if (!user.isActive()) {
            throw new BadRequestException(
                    "Account non attivo"
            );
        }
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);


        String token = jwtTools.createToken(user);
        return new AuthResponseDTO(token);
    }

    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Token non valido"));

        if (user.getEmailVerificationTokenExpiry() == null
                || user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Il link di verifica è scaduto, richiedine uno nuovo");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);

        return "Email verificata con successo";
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isEmailVerified()) return;

            String verificationToken = UUID.randomUUID().toString();
            user.setEmailVerificationToken(verificationToken);
            user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
            userRepository.save(user);

            String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
            mailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationLink);
        });
    }
}
