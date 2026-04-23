package com.f4ture.registrationserv.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpConfirmRequest {
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotBlank(message = "Код обязателен")
    private String code;
}
