package com.shashi.splitwise.settlement.domain;

import com.shashi.splitwise.group.domain.ExpenseGroup;
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
 * A recorded transfer between two group members. Reduces both parties'
 * net balances by {@code amountCents} when {@link com.shashi.splitwise.balance.application.BalanceService}
 * recomputes the per-user totals.
 */
@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false, updatable = false)
    private ExpenseGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user_id", nullable = false, updatable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user_id", nullable = false, updatable = false)
    private User toUser;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(length = 255)
    private String note;

    @Column(name = "settled_at", nullable = false)
    private Instant settledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Settlement() {
        // for JPA
    }

    public Settlement(ExpenseGroup group, User fromUser, User toUser,
                      long amountCents, String currencyCode,
                      String note, Instant settledAt) {
        this.group = group;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amountCents = amountCents;
        this.currencyCode = currencyCode;
        this.note = note;
        this.settledAt = settledAt;
    }

    public Long getId() { return id; }
    public ExpenseGroup getGroup() { return group; }
    public User getFromUser() { return fromUser; }
    public User getToUser() { return toUser; }
    public long getAmountCents() { return amountCents; }
    public String getCurrencyCode() { return currencyCode; }
    public String getNote() { return note; }
    public Instant getSettledAt() { return settledAt; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Settlement other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
