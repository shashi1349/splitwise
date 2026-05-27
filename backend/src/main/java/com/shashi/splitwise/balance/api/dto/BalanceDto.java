package com.shashi.splitwise.balance.api.dto;

/**
 * One member's net balance within a group.
 *
 * <ul>
 *   <li>{@code netCents > 0} — owed money by the group (creditor)</li>
 *   <li>{@code netCents < 0} — owes money to the group (debtor)</li>
 *   <li>{@code netCents == 0} — settled up</li>
 * </ul>
 *
 * The invariant {@code sum(netCents) == 0} holds across all members
 * because every expense contributes {@code +amount} to its payer and
 * {@code -share} to each participant, totalling zero.
 */
public record BalanceDto(
    Long userId,
    String displayName,
    long netCents,
    String currencyCode
) {}
