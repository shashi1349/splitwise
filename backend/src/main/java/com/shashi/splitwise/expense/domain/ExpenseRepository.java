package com.shashi.splitwise.expense.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Page of expenses for a group, ordered newest-first. The entity graph
     * eagerly loads payer, shares, and shares' user so the JSON serializer
     * doesn't trigger N+1 SELECTs.
     */
    @EntityGraph(attributePaths = {"payer", "shares", "shares.user"})
    @Query("""
        SELECT e FROM Expense e
        WHERE e.group.id = :groupId
        ORDER BY e.occurredAt DESC, e.id DESC
    """)
    Page<Expense> findPageByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * All expenses for a group with shares eagerly loaded — used by the
     * balance and settle-up calculations in subsequent modules.
     */
    @EntityGraph(attributePaths = {"payer", "shares", "shares.user"})
    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId ORDER BY e.occurredAt ASC, e.id ASC")
    List<Expense> findAllByGroupId(@Param("groupId") Long groupId);
}
