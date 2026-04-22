package com.f4ture.registrationserv.Controller;

import com.f4ture.registrationserv.DTO.RegisterRequest;
import com.f4ture.registrationserv.DTO.RegisterResponse;
import com.f4ture.registrationserv.Entity.User;
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

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        RegisterResponse body = new RegisterResponse(user.getEmail(), "Регистрация прошла успешно");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @ExceptionHandler(UserService.EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(UserService.EmailAlreadyExistsException e) {
        Map<String, String> body = new HashMap<>();
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> body = new HashMap<>();
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Некорректные данные");
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
