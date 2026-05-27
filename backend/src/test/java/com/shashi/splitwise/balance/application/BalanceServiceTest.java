package com.shashi.splitwise.balance.application;

import com.shashi.splitwise.balance.api.dto.BalanceDto;
import com.shashi.splitwise.expense.domain.Expense;
import com.shashi.splitwise.expense.domain.ExpenseRepository;
import com.shashi.splitwise.expense.domain.ExpenseShare;
import com.shashi.splitwise.expense.domain.SplitType;
import com.shashi.splitwise.group.application.GroupService;
import com.shashi.splitwise.group.domain.ExpenseGroup;
import com.shashi.splitwise.group.domain.GroupMember;
import com.shashi.splitwise.group.domain.GroupMemberRepository;
import com.shashi.splitwise.group.domain.MemberRole;
import com.shashi.splitwise.settlement.domain.Settlement;
import com.shashi.splitwise.settlement.domain.SettlementRepository;
import com.shashi.splitwise.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceService")
class BalanceServiceTest {

    @Mock ExpenseRepository expenses;
    @Mock SettlementRepository settlements;
    @Mock GroupMemberRepository members;
    @Mock GroupService groupService;

    @InjectMocks BalanceService service;

    private User alice;
    private User bob;
    private User carol;
    private ExpenseGroup group;

    @BeforeEach
    void setUp() {
        alice = user(1L, "Alice");
        bob = user(2L, "Bob");
        carol = user(3L, "Carol");
        group = group(10L);
    }

    @Test
    @DisplayName("computeBalanceMap — single expense credits payer and debits each share owner")
    void computeBalanceMap_singleExpense() {
        Expense e = expense(group, alice, 30000L, List.of(
            share(alice, 10000L), share(bob, 10000L), share(carol, 10000L)));
        when(expenses.findAllByGroupId(10L)).thenReturn(List.of(e));
        when(settlements.findAllByGroupId(10L)).thenReturn(List.of());

        Map<Long, Long> net = service.computeBalanceMap(10L);

        assertThat(net).containsEntry(1L, 20000L).containsEntry(2L, -10000L).containsEntry(3L, -10000L);
    }

    @Test
    @DisplayName("computeBalanceMap — combines effects across multiple expenses")
    void computeBalanceMap_combinesAcrossExpenses() {
        Expense first = expense(group, alice, 30000L, List.of(
            share(alice, 10000L), share(bob, 10000L), share(carol, 10000L)));
        Expense second = expense(group, bob, 6000L, List.of(
            share(alice, 2000L), share(bob, 2000L), share(carol, 2000L)));
        when(expenses.findAllByGroupId(10L)).thenReturn(List.of(first, second));
        when(settlements.findAllByGroupId(10L)).thenReturn(List.of());

        Map<Long, Long> net = service.computeBalanceMap(10L);

        assertThat(net).containsEntry(1L, 18000L).containsEntry(2L, -6000L).containsEntry(3L, -12000L);
        assertThat(net.values().stream().mapToLong(Long::longValue).sum()).isZero();
    }

    @Test
    @DisplayName("computeBalanceMap — settlements move both parties toward zero")
    void computeBalanceMap_subtractsSettlements() {
        Expense e = expense(group, alice, 30000L, List.of(
            share(alice, 10000L), share(bob, 10000L), share(carol, 10000L)));
        Settlement paid = settlement(group, carol, alice, 10000L);
        when(expenses.findAllByGroupId(10L)).thenReturn(List.of(e));
        when(settlements.findAllByGroupId(10L)).thenReturn(List.of(paid));

        Map<Long, Long> net = service.computeBalanceMap(10L);

        assertThat(net).containsEntry(1L, 10000L)   // 20000 - 10000
            .containsEntry(2L, -10000L)              // unchanged
            .containsEntry(3L, 0L);                  // -10000 + 10000
        assertThat(net.values().stream().mapToLong(Long::longValue).sum()).isZero();
    }

    @Test
    @DisplayName("computeBalanceMap — sum-of-balances stays at zero in mixed scenarios")
    void computeBalanceMap_preservesSumZero() {
        Expense e1 = expense(group, alice, 12000L, List.of(
            share(alice, 6000L), share(bob, 6000L)));
        Expense e2 = expense(group, bob, 8000L, List.of(
            share(alice, 4000L), share(bob, 4000L)));
        Settlement s = settlement(group, alice, bob, 1000L);
        when(expenses.findAllByGroupId(10L)).thenReturn(List.of(e1, e2));
        when(settlements.findAllByGroupId(10L)).thenReturn(List.of(s));

        Map<Long, Long> net = service.computeBalanceMap(10L);
        assertThat(net.values().stream().mapToLong(Long::longValue).sum()).isZero();
    }

    @Test
    @DisplayName("computeBalances — pads zero rows for members with no activity and authorises caller")
    void computeBalances_paddsInactiveMembers() {
        when(groupService.requireMembership(10L, 1L)).thenReturn(group);
        when(expenses.findAllByGroupId(10L)).thenReturn(List.of());
        when(settlements.findAllByGroupId(10L)).thenReturn(List.of());
        when(members.findAllForGroup(10L)).thenReturn(List.of(
            member(group, alice), member(group, bob), member(group, carol)));

        List<BalanceDto> rows = service.computeBalances(10L, 1L);

        assertThat(rows).hasSize(3);
        assertThat(rows).allMatch(r -> r.netCents() == 0L);
        assertThat(rows).allMatch(r -> "INR".equals(r.currencyCode()));
    }

    @Test
    @DisplayName("computeBalances — sorts creditors before debtors, ties broken alphabetically")
    void computeBalances_sortsCreditorsFirst() {
        Expense e = expense(group, alice, 30000L, List.of(
            share(alice, 10000L), share(bob, 10000L), share(carol, 10000L)));
        when(groupService.requireMembership(10L, 1L)).thenReturn(group);
        when(expenses.findAllByGroupId(10L)).thenReturn(List.of(e));
        when(settlements.findAllByGroupId(10L)).thenReturn(List.of());
        when(members.findAllForGroup(10L)).thenReturn(List.of(
            member(group, alice), member(group, bob), member(group, carol)));

        List<BalanceDto> rows = service.computeBalances(10L, 1L);

        assertThat(rows).extracting(BalanceDto::displayName).containsExactly("Alice", "Bob", "Carol");
        assertThat(rows.get(0).netCents()).isEqualTo(20000L);
        assertThat(rows.get(1).netCents()).isEqualTo(-10000L);
        assertThat(rows.get(2).netCents()).isEqualTo(-10000L);
    }

    // ---------- helpers ----------

    private static User user(long id, String name) {
        User u = new User(name.toLowerCase() + "@example.com", name, "hash");
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private static ExpenseGroup group(long id) {
        ExpenseGroup g = new ExpenseGroup("Trip", "INR", null);
        ReflectionTestUtils.setField(g, "id", id);
        return g;
    }

    private static Expense expense(ExpenseGroup group, User payer, long total,
                                   List<ExpenseShare> shares) {
        Expense e = new Expense(group, payer, "x", total, "INR", SplitType.EQUAL, Instant.now());
        for (ExpenseShare share : shares) {
            ReflectionTestUtils.setField(share, "expense", e);
            e.addShare(share);
        }
        return e;
    }

    private static ExpenseShare share(User user, long shareCents) {
        return new ExpenseShare(null, user, shareCents);
    }

    private static Settlement settlement(ExpenseGroup group, User from, User to, long amountCents) {
        return new Settlement(group, from, to, amountCents, "INR", null, Instant.now());
    }

    private static GroupMember member(ExpenseGroup group, User user) {
        return new GroupMember(group, user, MemberRole.MEMBER);
    }
}
