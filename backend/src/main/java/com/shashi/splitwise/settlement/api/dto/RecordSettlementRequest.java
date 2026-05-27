package com.shashi.splitwise.settlement.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RecordSettlementRequest(
    @NotNull Long fromUserId,

    @NotNull Long toUserId,

    @NotNull
    @DecimalMin(value = "0.01", message = "must be at least 0.01")
    @Digits(integer = 12, fraction = 2)
    BigDecimal amount,

    @Size(max = 255)
    String note
) {}
