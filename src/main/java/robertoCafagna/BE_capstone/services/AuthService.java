package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.AuthResponseDTO;
import robertoCafagna.BE_capstone.DTO.LoginRequestDTO;
import robertoCafagna.BE_capstone.DTO.RegisterRequestDTO;
import robertoCafagna.BE_capstone.config.DefaultImageConfig;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.repositories.UserRepository;
import robertoCafagna.BE_capstone.security.JWTTools;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTTools jwtTools;
    private final AuthenticationManager authenticationManager;
    private final DefaultImageConfig defaultImageConfig;

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

        userRepository.save(user);
        return "Utente registrato con successo";
    }

    public AuthResponseDTO login(LoginRequestDTO body) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.username(), body.password())
        );

        User user = (User) authentication.getPrincipal();

        assert user != null;
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        if (!user.isActive()) {
            throw new BadRequestException(
                    "Account non attivo"
            );
        }

        String token = jwtTools.createToken(user);
        return new AuthResponseDTO(token);
    }
}
