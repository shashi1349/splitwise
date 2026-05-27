package com.shashi.splitwise.group.api.dto;

import com.shashi.splitwise.group.domain.MemberRole;

import java.time.Instant;

/**
 * Lightweight projection for "my groups" listing. Populated by a JPQL
 * constructor expression in {@code GroupMemberRepository#findSummariesForUser}.
 */
public record GroupSummary(
    Long id,
    String name,
    String currencyCode,
    MemberRole myRole,
    Instant createdAt,
    Long memberCount
) {}
