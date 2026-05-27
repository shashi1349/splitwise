package com.shashi.splitwise.settlement.api.dto;

/**
 * One row of the settle-up suggestion: {@code from} pays {@code amountCents}
 * to {@code to}. These are advisory — clients still POST to /settlements
 * to record the actual transfer.
 */
public record TransferDto(
    Long fromUserId,
    String fromUserDisplayName,
    Long toUserId,
    String toUserDisplayName,
    long amountCents,
    String currencyCode
) {}
