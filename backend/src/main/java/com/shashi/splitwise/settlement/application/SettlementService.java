package com.shashi.splitwise.settlement.application;

import com.shashi.splitwise.balance.application.BalanceService;
import com.shashi.splitwise.common.error.BadRequestException;
import com.shashi.splitwise.group.application.GroupService;
import com.shashi.splitwise.group.domain.ExpenseGroup;
import com.shashi.splitwise.group.domain.GroupMemberRepository;
import com.shashi.splitwise.settlement.api.dto.RecordSettlementRequest;
import com.shashi.splitwise.settlement.api.dto.SettlementDto;
import com.shashi.splitwise.settlement.api.dto.TransferDto;
import com.shashi.splitwise.settlement.domain.Settlement;
import com.shashi.splitwise.settlement.domain.SettlementRepository;
import com.shashi.splitwise.user.domain.User;
import com.shashi.splitwise.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    private final SettlementRepository settlements;
    private final GroupMemberRepository members;
    private final UserRepository users;
    private final GroupService groupService;
    private final BalanceService balances;

    public SettlementService(SettlementRepository settlements,
                             GroupMemberRepository members,
                             UserRepository users,
                             GroupService groupService,
                             BalanceService balances) {
        this.settlements = settlements;
        this.members = members;
        this.users = users;
        this.groupService = groupService;
        this.balances = balances;
    }

    @Transactional(readOnly = true)
    public List<TransferDto> suggestSettlements(Long groupId, Long actingUserId) {
        ExpenseGroup group = groupService.requireMembership(groupId, actingUserId);
        Map<Long, Long> net = balances.computeBalanceMap(groupId);
        Map<Long, String> displayNameByUserId = members.findAllForGroup(groupId).stream()
            .collect(Collectors.toMap(m -> m.getUser().getId(), m -> m.getUser().getDisplayName()));
        return DebtSimplifier.simplify(net).stream()
            .map(t -> new TransferDto(
                t.fromUserId(), displayNameByUserId.get(t.fromUserId()),
                t.toUserId(), displayNameByUserId.get(t.toUserId()),
                t.amountCents(), group.getCurrencyCode()))
            .toList();
    }

    @Transactional
    public SettlementDto recordSettlement(Long groupId, Long actingUserId, RecordSettlementRequest req) {
        ExpenseGroup group = groupService.requireMembership(groupId, actingUserId);
        if (req.fromUserId().equals(req.toUserId())) {
            throw new BadRequestException("Cannot settle a transfer from a user to themselves.");
        }
        Set<Long> memberIds = members.findAllForGroup(groupId).stream()
            .map(m -> m.getUser().getId())
            .collect(Collectors.toSet());
        if (!memberIds.contains(req.fromUserId()) || !memberIds.contains(req.toUserId())) {
            throw new BadRequestException("Both parties must be members of this group.");
        }
        long cents = toCents(req.amount());
        if (cents <= 0) {
            throw new BadRequestException("Amount must be positive.");
        }

        User from = users.findById(req.fromUserId())
            .orElseThrow(() -> new BadRequestException("Payer not found."));
        User to = users.findById(req.toUserId())
            .orElseThrow(() -> new BadRequestException("Recipient not found."));

        String note = (req.note() == null || req.note().isBlank()) ? null : req.note().trim();
        Settlement saved = settlements.save(new Settlement(
            group, from, to, cents, group.getCurrencyCode(), note, Instant.now()));
        return SettlementDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SettlementDto> listSettlements(Long groupId, Long actingUserId) {
        groupService.requireMembership(groupId, actingUserId);
        return settlements.findAllByGroupId(groupId).stream()
            .map(SettlementDto::from)
            .toList();
    }

    /** Echo of the helper used inside ExpenseService — kept private so
     *  it doesn't leak into the public API surface. */
    private static long toCents(BigDecimal amount) {
        try {
            return amount.movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new BadRequestException("Amount has more than 2 decimal places.");
        }
    }
}
