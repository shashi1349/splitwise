package com.shashi.splitwise.settlement.application;

import com.shashi.splitwise.settlement.application.DebtSimplifier.Transfer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DebtSimplifier")
class DebtSimplifierTest {

    @Test
    @DisplayName("returns no transfers for an empty input")
    void empty() {
        assertThat(DebtSimplifier.simplify(Map.of())).isEmpty();
    }

    @Test
    @DisplayName("returns no transfers when every balance is zero")
    void allZero() {
        Map<Long, Long> input = Map.of(1L, 0L, 2L, 0L, 3L, 0L);
        assertThat(DebtSimplifier.simplify(input)).isEmpty();
    }

    @Test
    @DisplayName("two-party debt resolves with a single transfer")
    void twoParty() {
        Map<Long, Long> input = Map.of(1L, 50_00L, 2L, -50_00L);
        List<Transfer> transfers = DebtSimplifier.simplify(input);
        assertThat(transfers).hasSize(1);
        Transfer t = transfers.get(0);
        assertThat(t.fromUserId()).isEqualTo(2L);
        assertThat(t.toUserId()).isEqualTo(1L);
        assertThat(t.amountCents()).isEqualTo(50_00L);
    }

    @Test
    @DisplayName("one creditor + two debtors yields exactly two transfers")
    void oneCreditorTwoDebtors() {
        Map<Long, Long> input = new LinkedHashMap<>();
        input.put(1L, 18_000L);
        input.put(2L, -6_000L);
        input.put(3L, -12_000L);
        List<Transfer> transfers = DebtSimplifier.simplify(input);
        assertThat(transfers).hasSize(2);
        // Largest debtor (Carol = -12000) goes first, paying 12000 to Alice.
        assertThat(transfers.get(0).fromUserId()).isEqualTo(3L);
        assertThat(transfers.get(0).toUserId()).isEqualTo(1L);
        assertThat(transfers.get(0).amountCents()).isEqualTo(12_000L);
        assertThat(transfers.get(1).fromUserId()).isEqualTo(2L);
        assertThat(transfers.get(1).toUserId()).isEqualTo(1L);
        assertThat(transfers.get(1).amountCents()).isEqualTo(6_000L);
    }

    @Test
    @DisplayName("transfer count is at most N-1 for N non-zero balances")
    void transferCountAtMostNMinus1() {
        // 5 non-zero balances summing to zero
        Map<Long, Long> input = Map.of(
            1L, 50_00L,
            2L, 30_00L,
            3L, -20_00L,
            4L, -40_00L,
            5L, -20_00L);
        List<Transfer> transfers = DebtSimplifier.simplify(input);
        long nonZero = input.values().stream().filter(v -> v != 0L).count();
        assertThat(transfers.size()).isLessThanOrEqualTo((int) nonZero - 1);
    }

    @Test
    @DisplayName("sum of transfer amounts equals the total positive balance")
    void sumInvariant() {
        Map<Long, Long> input = Map.of(
            1L, 100_00L,
            2L, 200_00L,
            3L, -150_00L,
            4L, -100_00L,
            5L, -50_00L);
        List<Transfer> transfers = DebtSimplifier.simplify(input);
        long sumPaid = transfers.stream().mapToLong(Transfer::amountCents).sum();
        long sumPositive = input.values().stream().filter(v -> v > 0).mapToLong(Long::longValue).sum();
        assertThat(sumPaid).isEqualTo(sumPositive);
    }

    @Test
    @DisplayName("output is deterministic across runs (heap tie-broken by userId)")
    void deterministic() {
        Map<Long, Long> input = Map.of(
            1L, 50_00L,
            2L, 50_00L,
            3L, -50_00L,
            4L, -50_00L);
        List<Transfer> first = DebtSimplifier.simplify(input);
        List<Transfer> second = DebtSimplifier.simplify(input);
        assertThat(second).containsExactlyElementsOf(first);
    }

    @Test
    @DisplayName("handles a chained debt by cancelling intermediate hops")
    void chainedDebtSimplifies() {
        // A owes B 100, B owes C 100, so net A=-100, B=0, C=+100 -> single A->C 100
        Map<Long, Long> input = Map.of(1L, -100_00L, 2L, 0L, 3L, 100_00L);
        List<Transfer> transfers = DebtSimplifier.simplify(input);
        assertThat(transfers).hasSize(1);
        assertThat(transfers.get(0).fromUserId()).isEqualTo(1L);
        assertThat(transfers.get(0).toUserId()).isEqualTo(3L);
        assertThat(transfers.get(0).amountCents()).isEqualTo(100_00L);
    }
}
