package com.shashi.splitwise.balance.application;

import com.shashi.splitwise.balance.api.dto.BalanceDto;
import com.shashi.splitwise.expense.domain.Expense;
import com.shashi.splitwise.expense.domain.ExpenseRepository;
import com.shashi.splitwise.expense.domain.ExpenseShare;
import com.shashi.splitwise.group.application.GroupService;
import com.shashi.splitwise.group.domain.ExpenseGroup;
import com.shashi.splitwise.group.domain.GroupMember;
import com.shashi.splitwise.group.domain.GroupMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes per-user net balances within a group from the expense ledger.
 *
 * <p>Module 5 considers only expenses; Module 6 extends this service to
 * also subtract recorded settlements via {@code subtractSettlements}.</p>
 */
@Service
public class BalanceService {

    private final ExpenseRepository expenses;
    private final GroupMemberRepository members;
    private final GroupService groupService;

    public BalanceService(ExpenseRepository expenses,
                          GroupMemberRepository members,
                          GroupService groupService) {
        this.expenses = expenses;
        this.members = members;
        this.groupService = groupService;
    }

    /**
     * Net balance per user for a group. Members with no recorded activity
     * are present in the returned map only if at least one expense
     * touched them; pad with zeros at the call site if you need a row
     * per member.
     */
    @Transactional(readOnly = true)
    public Map<Long, Long> computeBalanceMap(Long groupId) {
        Map<Long, Long> net = new HashMap<>();
        for (Expense e : expenses.findAllByGroupId(groupId)) {
            net.merge(e.getPayer().getId(), e.getAmountCents(), Long::sum);
            for (ExpenseShare s : e.getShares()) {
                net.merge(s.getUser().getId(), -s.getShareCents(), Long::sum);
            }
        }
        return net;
    }

    /**
     * Public API for the {@code GET /groups/{id}/balances} endpoint.
     * Authorises the caller, computes raw balances, fills zero rows for
     * members with no activity, and returns rows sorted creditors-first.
     */
    @Transactional(readOnly = true)
    public List<BalanceDto> computeBalances(Long groupId, Long actingUserId) {
        ExpenseGroup group = groupService.requireMembership(groupId, actingUserId);
        Map<Long, Long> net = computeBalanceMap(groupId);
        List<GroupMember> memberRows = members.findAllForGroup(groupId);
        return memberRows.stream()
            .map(m -> new BalanceDto(
                m.getUser().getId(),
                m.getUser().getDisplayName(),
                net.getOrDefault(m.getUser().getId(), 0L),
                group.getCurrencyCode()))
            .sorted(
                Comparator.comparingLong(BalanceDto::netCents).reversed()
                    .thenComparing(BalanceDto::displayName))
            .toList();
    }
}
