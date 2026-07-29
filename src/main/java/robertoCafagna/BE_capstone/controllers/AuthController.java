package robertoCafagna.BE_capstone.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.*;
import robertoCafagna.BE_capstone.services.AuthService;
import robertoCafagna.BE_capstone.services.PasswordResetService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;


    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO body) {
        passwordResetService.requestReset(body.email());
        return ResponseEntity.ok("Se l'indirizzo email è registrato, riceverai a breve un link per reimpostare la password");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO body) {
        passwordResetService.resetPassword(body.token(), body.newPassword());
        return ResponseEntity.ok("Password reimpostata con successo");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestBody @Valid ForgotPasswordRequestDTO body) {
        authService.resendVerificationEmail(body.email());
        return ResponseEntity.ok("Se l'indirizzo email è registrato e non ancora verificato, riceverai un nuovo link");
    }

}
