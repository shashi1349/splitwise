package com.shashi.splitwise.expense.domain;

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
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

@Entity
@Table(
    name = "expense_shares",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_expense_shares_expense_user",
        columnNames = {"expense_id", "user_id"}
    )
)
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false, updatable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "share_cents", nullable = false)
    private long shareCents;

    protected ExpenseShare() {
        // for JPA
    }

    public ExpenseShare(Expense expense, User user, long shareCents) {
        this.expense = expense;
        this.user = user;
        this.shareCents = shareCents;
    }

    public Long getId() { return id; }
    public Expense getExpense() { return expense; }
    public User getUser() { return user; }
    public long getShareCents() { return shareCents; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseShare other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
