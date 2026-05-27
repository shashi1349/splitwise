package com.shashi.splitwise.group.api.dto;

import com.shashi.splitwise.group.domain.ExpenseGroup;

import java.time.Instant;
import java.util.List;

public record GroupDetail(
    Long id,
    String name,
    String currencyCode,
    Long createdById,
    Instant createdAt,
    List<MemberDto> members
) {

    public static GroupDetail of(ExpenseGroup group, List<MemberDto> members) {
        return new GroupDetail(
            group.getId(),
            group.getName(),
            group.getCurrencyCode(),
            group.getCreatedBy().getId(),
            group.getCreatedAt(),
            members
        );
    }
}
