package com.shashi.splitwise.group.api.dto;

import com.shashi.splitwise.group.domain.GroupMember;
import com.shashi.splitwise.group.domain.MemberRole;

import java.time.Instant;

public record MemberDto(
    Long userId,
    String email,
    String displayName,
    MemberRole role,
    Instant joinedAt
) {

    public static MemberDto from(GroupMember m) {
        return new MemberDto(
            m.getUser().getId(),
            m.getUser().getEmail(),
            m.getUser().getDisplayName(),
            m.getRole(),
            m.getJoinedAt()
        );
    }
}
