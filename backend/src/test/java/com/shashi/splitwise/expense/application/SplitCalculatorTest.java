package com.shashi.splitwise.expense.application;

import com.shashi.splitwise.common.error.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SplitCalculator")
class SplitCalculatorTest {

    @Test
    @DisplayName("EQUAL — divides evenly when there is no remainder")
    void equal_evenly() {
        Map<Long, Long> shares = SplitCalculator.equal(900L, List.of(1L, 2L, 3L));
        assertThat(shares).containsExactlyInAnyOrderEntriesOf(Map.of(1L, 300L, 2L, 300L, 3L, 300L));
        assertThat(shares.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(900L);
    }

    @Test
    @DisplayName("EQUAL — remainder is allocated to the smallest userIds first")
    void equal_remainderGoesToSmallestUserIds() {
        Map<Long, Long> shares = SplitCalculator.equal(100L, List.of(3L, 1L, 2L));
        assertThat(shares).containsEntry(1L, 34L)
            .containsEntry(2L, 33L)
            .containsEntry(3L, 33L);
        assertThat(shares.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(100L);
    }

    @Test
    @DisplayName("EQUAL — single participant receives the whole total")
    void equal_singleParticipant() {
        assertThat(SplitCalculator.equal(7777L, List.of(42L)))
            .containsExactlyEntriesOf(Map.of(42L, 7777L));
    }

    @Test
    @DisplayName("EQUAL — preserves the sum-equals-total invariant for awkward inputs")
    void equal_sumInvariantHolds() {
        Map<Long, Long> shares = SplitCalculator.equal(7L, List.of(1L, 2L, 3L, 4L));
        assertThat(shares).containsEntry(1L, 2L)
            .containsEntry(2L, 2L)
            .containsEntry(3L, 2L)
            .containsEntry(4L, 1L);
        assertThat(shares.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(7L);
    }

    @Test
    @DisplayName("EQUAL — rejects non-positive total")
    void equal_rejectsNonPositiveTotal() {
        assertThatThrownBy(() -> SplitCalculator.equal(0L, List.of(1L, 2L)))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("positive");
        assertThatThrownBy(() -> SplitCalculator.equal(-1L, List.of(1L)))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("EQUAL — rejects empty participant list")
    void equal_rejectsEmpty() {
        assertThatThrownBy(() -> SplitCalculator.equal(100L, List.of()))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("participant");
    }

    @Test
    @DisplayName("EQUAL — rejects duplicate participants")
    void equal_rejectsDuplicates() {
        assertThatThrownBy(() -> SplitCalculator.equal(100L, List.of(1L, 1L, 2L)))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Duplicate");
    }

    @Test
    @DisplayName("EXACT — accepts shares whose sum equals the total")
    void exact_acceptsValid() {
        Map<Long, BigDecimal> input = new LinkedHashMap<>();
        input.put(1L, new BigDecimal("30.00"));
        input.put(2L, new BigDecimal("20.00"));
        input.put(3L, new BigDecimal("25.00"));
        Map<Long, Long> out = SplitCalculator.exact(7500L, input);
        assertThat(out).containsEntry(1L, 3000L).containsEntry(2L, 2000L).containsEntry(3L, 2500L);
        assertThat(out.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(7500L);
    }

    @Test
    @DisplayName("EXACT — rejects sum that does not match the total")
    void exact_rejectsMismatch() {
        Map<Long, BigDecimal> input = Map.of(1L, new BigDecimal("50.00"), 2L, new BigDecimal("40.00"));
        assertThatThrownBy(() -> SplitCalculator.exact(10000L, input))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("100.00")
            .hasMessageContaining("90.00");
    }

    @Test
    @DisplayName("EXACT — rejects negative shares")
    void exact_rejectsNegative() {
        Map<Long, BigDecimal> input = Map.of(1L, new BigDecimal("-1.00"), 2L, new BigDecimal("11.00"));
        assertThatThrownBy(() -> SplitCalculator.exact(1000L, input))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("EXACT — rejects shares with more than 2 decimal places")
    void exact_rejectsExtraDecimals() {
        Map<Long, BigDecimal> input = Map.of(1L, new BigDecimal("10.123"));
        assertThatThrownBy(() -> SplitCalculator.exact(1012L, input))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("decimal");
    }

    @Test
    @DisplayName("EXACT — rejects empty share map")
    void exact_rejectsEmpty() {
        assertThatThrownBy(() -> SplitCalculator.exact(100L, Map.of()))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("PERCENT — 33.33/33.33/33.34 distributes the rounding cent to the smallest userId")
    void percent_thirds() {
        Map<Long, BigDecimal> input = new LinkedHashMap<>();
        input.put(1L, new BigDecimal("33.33"));
        input.put(2L, new BigDecimal("33.33"));
        input.put(3L, new BigDecimal("33.34"));
        Map<Long, Long> out = SplitCalculator.percent(1000L, input);
        assertThat(out).containsEntry(1L, 334L).containsEntry(2L, 333L).containsEntry(3L, 333L);
        assertThat(out.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("PERCENT — clean 50/50 with no remainder")
    void percent_50_50() {
        Map<Long, BigDecimal> input = Map.of(1L, new BigDecimal("50.00"), 2L, new BigDecimal("50.00"));
        Map<Long, Long> out = SplitCalculator.percent(10000L, input);
        assertThat(out).containsEntry(1L, 5000L).containsEntry(2L, 5000L);
    }

    @Test
    @DisplayName("PERCENT — rejects sums that are not exactly 100.00")
    void percent_rejectsBadSum() {
        Map<Long, BigDecimal> input = Map.of(1L, new BigDecimal("50.00"), 2L, new BigDecimal("50.50"));
        assertThatThrownBy(() -> SplitCalculator.percent(10000L, input))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("100.50");
    }

    @Test
    @DisplayName("PERCENT — rejects negative percents")
    void percent_rejectsNegative() {
        Map<Long, BigDecimal> input = Map.of(1L, new BigDecimal("-10.00"), 2L, new BigDecimal("110.00"));
        assertThatThrownBy(() -> SplitCalculator.percent(10000L, input))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("PERCENT — allows participants at 0%")
    void percent_zeroAllowed() {
        Map<Long, BigDecimal> input = new LinkedHashMap<>();
        input.put(1L, new BigDecimal("100.00"));
        input.put(2L, new BigDecimal("0.00"));
        Map<Long, Long> out = SplitCalculator.percent(5000L, input);
        assertThat(out).containsEntry(1L, 5000L).containsEntry(2L, 0L);
    }

    @ParameterizedTest(name = "EQUAL invariant — {0} cents across {1} users")
    @CsvSource({
        "100, 3", "999, 7", "1, 1", "13, 5", "10000, 4", "31415, 11"
    })
    @DisplayName("EQUAL — sum-of-shares always equals the total")
    void equal_invariantSumProperty(long total, int n) {
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, n).boxed().toList();
        Map<Long, Long> out = SplitCalculator.equal(total, ids);
        assertThat(out.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(total);
        assertThat(out.values()).allMatch(v -> v >= 0);
    }
}
