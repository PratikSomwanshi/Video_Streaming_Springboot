package com.wanda.utils.exceptions.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String email;
    private String username;
    private String token;

    public LoginResponse(String email, String username, String token) {
        this.email = email;
        this.username = username;
        this.token = token;
    }
}
