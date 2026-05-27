package com.shashi.splitwise.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Email(message = "must be a valid email")
    @NotBlank
    @Size(max = 255)
    String email,

    @NotBlank
    @Size(min = 1, max = 100, message = "must be 1-100 characters")
    String displayName,

    @NotBlank
    @Size(min = 8, max = 100, message = "must be at least 8 characters")
    String password
) {}
