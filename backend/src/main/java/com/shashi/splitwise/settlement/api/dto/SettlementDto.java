package com.shashi.splitwise.settlement.api.dto;

import com.shashi.splitwise.settlement.domain.Settlement;

import java.time.Instant;

public record SettlementDto(
    Long id,
    Long groupId,
    Long fromUserId,
    String fromUserDisplayName,
    Long toUserId,
    String toUserDisplayName,
    long amountCents,
    String currencyCode,
    String note,
    Instant settledAt,
    Instant createdAt
) {

    public static SettlementDto from(Settlement s) {
        return new SettlementDto(
            s.getId(),
            s.getGroup().getId(),
            s.getFromUser().getId(),
            s.getFromUser().getDisplayName(),
            s.getToUser().getId(),
            s.getToUser().getDisplayName(),
            s.getAmountCents(),
            s.getCurrencyCode(),
            s.getNote(),
            s.getSettledAt(),
            s.getCreatedAt()
        );
    }
}
