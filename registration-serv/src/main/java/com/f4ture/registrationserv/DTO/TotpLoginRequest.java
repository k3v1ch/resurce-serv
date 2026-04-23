package com.f4ture.registrationserv.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpLoginRequest {
    @NotBlank(message = "Токен обязателен")
    private String preAuthToken;

    @NotBlank(message = "Код обязателен")
    private String code;
}
