package com.busgo.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    @NotBlank
    private String username; // email or mobile
    @NotBlank
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
