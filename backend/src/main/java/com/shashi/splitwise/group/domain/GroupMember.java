package com.shashi.splitwise.group.domain;

import com.shashi.splitwise.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

/**
 * Join entity binding a {@link User} to an {@link ExpenseGroup} with a role.
 * The unique constraint mirrors the schema-level guarantee: a user can
 * appear at most once in a given group.
 */
@Entity
@Table(
    name = "group_members",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_group_members_group_user",
        columnNames = {"group_id", "user_id"}
    )
)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false, updatable = false)
    private ExpenseGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected GroupMember() {
        // for JPA
    }

    public GroupMember(ExpenseGroup group, User user, MemberRole role) {
        this.group = group;
        this.user = user;
        this.role = role;
    }

    public Long getId() { return id; }
    public ExpenseGroup getGroup() { return group; }
    public User getUser() { return user; }
    public MemberRole getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMember other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
