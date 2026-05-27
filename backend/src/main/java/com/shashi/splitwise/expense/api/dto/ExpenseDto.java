package com.shashi.splitwise.expense.api.dto;

import com.shashi.splitwise.expense.domain.Expense;
import com.shashi.splitwise.expense.domain.SplitType;

import java.time.Instant;
import java.util.List;

public record ExpenseDto(
    Long id,
    Long groupId,
    Long payerId,
    String payerDisplayName,
    String description,
    long amountCents,
    String currencyCode,
    SplitType splitType,
    Instant occurredAt,
    Instant createdAt,
    List<ExpenseShareDto> shares
) {
    public static ExpenseDto from(Expense e) {
        return new ExpenseDto(
            e.getId(),
            e.getGroup().getId(),
            e.getPayer().getId(),
            e.getPayer().getDisplayName(),
            e.getDescription(),
            e.getAmountCents(),
            e.getCurrencyCode(),
            e.getSplitType(),
            e.getOccurredAt(),
            e.getCreatedAt(),
            e.getShares().stream().map(ExpenseShareDto::from).toList()
        );
    }
}
