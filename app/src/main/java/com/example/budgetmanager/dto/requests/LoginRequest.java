package com.example.budgetmanager.dto.requests;


/**
 * LOGIN REQUEST DTO - DEVELOPER GUIDE
 *
 * PURPOSE: Encapsulate data for login requests
 *
 * IMMUTABLE: Fields are final, only getters provided
 * CLEAN: No business logic, only validation rules
 */
public class LoginRequest {
    private final String email;
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}