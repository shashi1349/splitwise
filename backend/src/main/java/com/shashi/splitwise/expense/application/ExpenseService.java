package com.shashi.splitwise.expense.application;

import com.shashi.splitwise.common.error.BadRequestException;
import com.shashi.splitwise.expense.api.dto.CreateExpenseRequest;
import com.shashi.splitwise.expense.api.dto.ExpenseDto;
import com.shashi.splitwise.expense.api.dto.ShareInput;
import com.shashi.splitwise.expense.domain.Expense;
import com.shashi.splitwise.expense.domain.ExpenseRepository;
import com.shashi.splitwise.expense.domain.ExpenseShare;
import com.shashi.splitwise.group.application.GroupService;
import com.shashi.splitwise.group.domain.ExpenseGroup;
import com.shashi.splitwise.group.domain.GroupMemberRepository;
import com.shashi.splitwise.user.domain.User;
import com.shashi.splitwise.user.domain.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenses;
    private final GroupService groupService;
    private final GroupMemberRepository members;
    private final UserRepository users;

    public ExpenseService(ExpenseRepository expenses,
                          GroupService groupService,
                          GroupMemberRepository members,
                          UserRepository users) {
        this.expenses = expenses;
        this.groupService = groupService;
        this.members = members;
        this.users = users;
    }

    @Transactional
    public ExpenseDto createExpense(Long groupId, Long actingUserId, CreateExpenseRequest req) {
        ExpenseGroup group = groupService.requireMembership(groupId, actingUserId);

        long totalCents = toCents(req.amount());

        if (req.shares() == null || req.shares().isEmpty()) {
            throw new BadRequestException("At least one participant is required.");
        }
        Set<Long> participantIds = req.shares().stream()
            .map(ShareInput::userId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (participantIds.size() != req.shares().size()) {
            throw new BadRequestException("Duplicate participants are not allowed.");
        }

        Set<Long> memberIds = members.findAllForGroup(groupId).stream()
            .map(m -> m.getUser().getId())
            .collect(Collectors.toSet());
        if (!memberIds.contains(req.payerId())) {
            throw new BadRequestException("Payer must be a member of the group.");
        }
        if (!memberIds.containsAll(participantIds)) {
            throw new BadRequestException("All participants must be members of the group.");
        }

        Map<Long, Long> shareCentsMap = computeShares(req, totalCents, participantIds);

        User payer = users.findById(req.payerId())
            .orElseThrow(() -> new BadRequestException("Payer not found."));

        Instant occurred = req.occurredAt() != null ? req.occurredAt() : Instant.now();
        Expense expense = new Expense(
            group, payer, req.description().trim(),
            totalCents, group.getCurrencyCode(), req.splitType(), occurred);

        Map<Long, User> userById = users.findAllByIdIn(participantIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));
        for (var e : shareCentsMap.entrySet()) {
            User u = userById.get(e.getKey());
            expense.addShare(new ExpenseShare(expense, u, e.getValue()));
        }

        Expense saved = expenses.save(expense);
        return ExpenseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseDto> listExpenses(Long groupId, Long actingUserId, Pageable pageable) {
        groupService.requireMembership(groupId, actingUserId);
        return expenses.findPageByGroupId(groupId, pageable).map(ExpenseDto::from);
    }

    private static Map<Long, Long> computeShares(CreateExpenseRequest req,
                                                 long totalCents,
                                                 Set<Long> participantIds) {
        return switch (req.splitType()) {
            case EQUAL -> SplitCalculator.equal(totalCents, List.copyOf(participantIds));
            case EXACT -> SplitCalculator.exact(totalCents, collectAmounts(req));
            case PERCENT -> SplitCalculator.percent(totalCents, collectPercents(req));
        };
    }

    private static Map<Long, BigDecimal> collectAmounts(CreateExpenseRequest req) {
        Map<Long, BigDecimal> map = new LinkedHashMap<>();
        for (ShareInput s : req.shares()) {
            if (s.amount() == null) {
                throw new BadRequestException(
                    "Each participant needs an exact amount when split type is EXACT.");
            }
            map.put(s.userId(), s.amount());
        }
        return map;
    }

    private static Map<Long, BigDecimal> collectPercents(CreateExpenseRequest req) {
        Map<Long, BigDecimal> map = new LinkedHashMap<>();
        for (ShareInput s : req.shares()) {
            if (s.percent() == null) {
                throw new BadRequestException(
                    "Each participant needs a percent when split type is PERCENT.");
            }
            map.put(s.userId(), s.percent());
        }
        return map;
    }

    private static long toCents(BigDecimal amount) {
        try {
            return amount.movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new BadRequestException("Amount has more than 2 decimal places.");
        }
    }
}
