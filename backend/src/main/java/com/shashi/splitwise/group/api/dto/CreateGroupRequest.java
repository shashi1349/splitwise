package com.shashi.splitwise.group.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
    @NotBlank
    @Size(min = 1, max = 100, message = "must be 1-100 characters")
    String name,

    @Pattern(regexp = "^$|^[A-Za-z]{3}$", message = "must be a 3-letter ISO 4217 code")
    String currencyCode
) {}
