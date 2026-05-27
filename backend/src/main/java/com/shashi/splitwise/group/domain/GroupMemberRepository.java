package com.shashi.splitwise.group.domain;

import com.shashi.splitwise.group.api.dto.GroupSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    /**
     * All memberships for a given group, with the user eagerly fetched so
     * subsequent {@code member.getUser()} access does not issue N+1 SELECTs.
     */
    @Query("""
        SELECT m FROM GroupMember m
        JOIN FETCH m.user
        WHERE m.group.id = :groupId
        ORDER BY m.joinedAt ASC
    """)
    List<GroupMember> findAllForGroup(@Param("groupId") Long groupId);

    /**
     * Aggregated view of every group the user belongs to, including a
     * correlated COUNT of members. One round-trip — no N+1.
     */
    @Query("""
        SELECT new com.shashi.splitwise.group.api.dto.GroupSummary(
            g.id,
            g.name,
            g.currencyCode,
            myM.role,
            g.createdAt,
            (SELECT COUNT(allM) FROM GroupMember allM WHERE allM.group = g))
        FROM ExpenseGroup g
        JOIN GroupMember myM ON myM.group = g
        WHERE myM.user.id = :userId
        ORDER BY g.createdAt DESC
    """)
    List<GroupSummary> findSummariesForUser(@Param("userId") Long userId);
}
