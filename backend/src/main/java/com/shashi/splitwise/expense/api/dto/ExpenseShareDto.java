package com.shashi.splitwise.expense.api.dto;

import com.shashi.splitwise.expense.domain.ExpenseShare;

public record ExpenseShareDto(
    Long userId,
    String displayName,
    long shareCents
) {
    public static ExpenseShareDto from(ExpenseShare s) {
        return new ExpenseShareDto(
            s.getUser().getId(),
            s.getUser().getDisplayName(),
            s.getShareCents()
        );
    }
}
