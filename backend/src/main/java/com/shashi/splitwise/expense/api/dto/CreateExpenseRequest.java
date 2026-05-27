package com.shashi.splitwise.expense.api.dto;

import com.shashi.splitwise.expense.domain.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreateExpenseRequest(
    @NotBlank @Size(max = 255)
    String description,

    @NotNull
    @DecimalMin(value = "0.01", message = "must be at least 0.01")
    @Digits(integer = 12, fraction = 2)
    BigDecimal amount,

    @NotNull
    Long payerId,

    @NotNull
    SplitType splitType,

    Instant occurredAt,

    @NotNull @Size(min = 1)
    List<@Valid ShareInput> shares
) {}
