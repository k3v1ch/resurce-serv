package com.f4ture.registrationserv.Controller;

import com.f4ture.registrationserv.DTO.*;
import com.f4ture.registrationserv.Entity.User;
import com.f4ture.registrationserv.Service.TotpService;
import com.f4ture.registrationserv.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;
    private final TotpService totpService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserService.LoginResult result = userService.login(request);
        if (result.requiresTotp()) {
            return ResponseEntity.ok(LoginResponse.preAuth(result.preAuthToken()));
        }
        return ResponseEntity.ok(LoginResponse.fullAuth(result.token()));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        String qrUri = totpService.buildQrUri(user.getEmail(), user.getTotpSecret());
        RegisterResponse body = new RegisterResponse(
                user.getEmail(),
                "Аккаунт создан. Отсканируйте QR-код и введите код для активации 2FA",
                user.getTotpSecret(),
                qrUri
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/2fa/confirm")
    public ResponseEntity<Map<String, String>> confirmTotp(@Valid @RequestBody TotpConfirmRequest request) {
        userService.confirmTotp(request.getEmail(), request.getCode());
        Map<String, String> body = new HashMap<>();
        body.put("message", "2FA успешно активирована");
        return ResponseEntity.ok(body);
    }

    @PostMapping("/2fa/login")
    public ResponseEntity<LoginResponse> loginWithTotp(@Valid @RequestBody TotpLoginRequest request) {
        String token = userService.loginWithTotp(request.getPreAuthToken(), request.getCode());
        return ResponseEntity.ok(LoginResponse.fullAuth(token));
    }

    @ExceptionHandler(UserService.InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(UserService.InvalidCredentialsException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UserService.EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(UserService.EmailAlreadyExistsException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(UserService.InvalidTotpCodeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTotp(UserService.InvalidTotpCodeException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UserService.TotpAlreadyEnabledException.class)
    public ResponseEntity<Map<String, String>> handleTotpAlreadyEnabled(UserService.TotpAlreadyEnabledException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Некорректные данные");
        return error(HttpStatus.BAD_REQUEST, msg);
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
