package com.f4ture.registrationserv.Service;

import com.f4ture.registrationserv.DTO.LoginRequest;
import com.f4ture.registrationserv.DTO.RegisterRequest;
import com.f4ture.registrationserv.Entity.User;
import com.f4ture.registrationserv.Repository.UserRepository;
import com.f4ture.registrationserv.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TotpService totpService;

    public record LoginResult(String token, String preAuthToken, boolean requiresTotp) {}

    public User register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }
        String totpSecret = totpService.generateSecret();
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .totpSecret(totpSecret)
                .build();
        return userRepository.save(user);
    }

    public void confirmTotp(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Пользователь не найден"));
        if (user.isTotpEnabled()) {
            throw new TotpAlreadyEnabledException("2FA уже активирована");
        }
        if (!totpService.verifyCode(user.getTotpSecret(), code)) {
            throw new InvalidTotpCodeException("Неверный код. Проверьте время на устройстве");
        }
        user.setTotpEnabled(true);
        userRepository.save(user);
    }

    public LoginResult login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Неверный email или пароль"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Неверный email или пароль");
        }
        if (user.isTotpEnabled()) {
            return new LoginResult(null, jwtService.generatePreAuthToken(email), true);
        }
        return new LoginResult(jwtService.generateToken(email), null, false);
    }

    public String loginWithTotp(String preAuthToken, String code) {
        String email;
        try {
            email = jwtService.extractEmailFromPreAuth(preAuthToken);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Сессия истекла, войдите снова");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Пользователь не найден"));
        if (!totpService.verifyCode(user.getTotpSecret(), code)) {
            throw new InvalidTotpCodeException("Неверный код аутентификатора");
        }
        return jwtService.generateToken(email);
    }

    public String getTotpQrUri(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Пользователь не найден"));
        return totpService.buildQrUri(user.getEmail(), user.getTotpSecret());
    }

    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) { super(message); }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) { super(message); }
    }

    public static class InvalidTotpCodeException extends RuntimeException {
        public InvalidTotpCodeException(String message) { super(message); }
    }

    public static class TotpAlreadyEnabledException extends RuntimeException {
        public TotpAlreadyEnabledException(String message) { super(message); }
    }
}
