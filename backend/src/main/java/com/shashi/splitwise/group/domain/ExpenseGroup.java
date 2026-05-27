package com.shashi.splitwise.group.domain;

import com.shashi.splitwise.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

/**
 * A group of users that share expenses. Mapped to {@code expense_groups}
 * (not {@code groups}, which is reserved in PostgreSQL). The class is
 * named {@code ExpenseGroup} to avoid the JPQL {@code GROUP} keyword.
 */
@Entity(name = "ExpenseGroup")
@Table(name = "expense_groups")
public class ExpenseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ExpenseGroup() {
        // for JPA
    }

    public ExpenseGroup(String name, String currencyCode, User createdBy) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCurrencyCode() { return currencyCode; }
    public User getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseGroup other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
