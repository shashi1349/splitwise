package com.shashi.splitwise.expense.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Per-participant input on a {@code POST /expenses} request.
 *
 * <ul>
 *   <li>For {@code EQUAL} — only {@code userId} matters.</li>
 *   <li>For {@code EXACT} — {@code amount} must be present.</li>
 *   <li>For {@code PERCENT} — {@code percent} must be present.</li>
 * </ul>
 */
public record ShareInput(
    @NotNull Long userId,

    @PositiveOrZero
    @Digits(integer = 12, fraction = 2)
    BigDecimal amount,

    @PositiveOrZero
    @Digits(integer = 3, fraction = 2)
    BigDecimal percent
) {}
