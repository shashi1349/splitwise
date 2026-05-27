package com.shashi.splitwise.expense.domain;

import com.shashi.splitwise.group.domain.ExpenseGroup;
import com.shashi.splitwise.user.domain.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false, updatable = false)
    private ExpenseGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false, length = 20)
    private SplitType splitType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(
        mappedBy = "expense",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<ExpenseShare> shares = new ArrayList<>();

    protected Expense() {
        // for JPA
    }

    public Expense(ExpenseGroup group, User payer, String description,
                   long amountCents, String currencyCode,
                   SplitType splitType, Instant occurredAt) {
        this.group = group;
        this.payer = payer;
        this.description = description;
        this.amountCents = amountCents;
        this.currencyCode = currencyCode;
        this.splitType = splitType;
        this.occurredAt = occurredAt;
    }

    public void addShare(ExpenseShare share) {
        shares.add(share);
    }

    public Long getId() { return id; }
    public ExpenseGroup getGroup() { return group; }
    public User getPayer() { return payer; }
    public String getDescription() { return description; }
    public long getAmountCents() { return amountCents; }
    public String getCurrencyCode() { return currencyCode; }
    public SplitType getSplitType() { return splitType; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ExpenseShare> getShares() { return Collections.unmodifiableList(shares); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Expense other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
