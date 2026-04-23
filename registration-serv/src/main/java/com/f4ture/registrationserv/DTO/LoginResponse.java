package com.f4ture.registrationserv.DTO;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Boolean requiresTotp;
    private String preAuthToken;

    public static LoginResponse fullAuth(String token) {
        LoginResponse r = new LoginResponse();
        r.token = token;
        return r;
    }

    public static LoginResponse preAuth(String preAuthToken) {
        LoginResponse r = new LoginResponse();
        r.requiresTotp = true;
        r.preAuthToken = preAuthToken;
        return r;
    }
}
