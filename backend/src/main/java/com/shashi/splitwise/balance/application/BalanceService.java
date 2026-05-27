package com.shashi.splitwise.balance.application;

import com.shashi.splitwise.balance.api.dto.BalanceDto;
import com.shashi.splitwise.expense.domain.Expense;
import com.shashi.splitwise.expense.domain.ExpenseRepository;
import com.shashi.splitwise.expense.domain.ExpenseShare;
import com.shashi.splitwise.group.application.GroupService;
import com.shashi.splitwise.group.domain.ExpenseGroup;
import com.shashi.splitwise.group.domain.GroupMember;
import com.shashi.splitwise.group.domain.GroupMemberRepository;
import com.shashi.splitwise.settlement.domain.Settlement;
import com.shashi.splitwise.settlement.domain.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes per-user net balances within a group from the expense ledger
 * minus any recorded settlements.
 */
@Service
public class BalanceService {

    private final ExpenseRepository expenses;
    private final SettlementRepository settlements;
    private final GroupMemberRepository members;
    private final GroupService groupService;

    public BalanceService(ExpenseRepository expenses,
                          SettlementRepository settlements,
                          GroupMemberRepository members,
                          GroupService groupService) {
        this.expenses = expenses;
        this.settlements = settlements;
        this.members = members;
        this.groupService = groupService;
    }

    /**
     * Net balance per user for a group.
     *
     * <p>For each expense the payer is credited {@code +amount} and each
     * participant is debited {@code -shareCents}. For each recorded
     * settlement the {@code from} user moves toward zero by
     * {@code +amount} and the {@code to} user moves toward zero by
     * {@code -amount} — i.e. paying off a debt cancels both sides.</p>
     *
     * <p>The invariant {@code sum(values) == 0} is preserved by every
     * operation.</p>
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
        for (Settlement s : settlements.findAllByGroupId(groupId)) {
            net.merge(s.getFromUser().getId(), s.getAmountCents(), Long::sum);
            net.merge(s.getToUser().getId(), -s.getAmountCents(), Long::sum);
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
