package com.shashi.splitwise.expense.application;

import com.shashi.splitwise.common.error.BadRequestException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure-function calculator that turns a (totalCents, splitType, participants)
 * tuple into a per-user share-cent map. Three invariants:
 *
 * <ol>
 *   <li>All math is integer math — never {@code double}.</li>
 *   <li>The sum of returned share-cents always equals {@code totalCents}.</li>
 *   <li>Rounding remainders are allocated deterministically to the
 *       participants with the smallest userIds, so two clients computing
 *       the same split agree byte-for-byte.</li>
 * </ol>
 */
public final class SplitCalculator {

    private static final long PERCENT_HUNDREDTHS = 100_00L; // 100.00 expressed as scale-2 long

    private SplitCalculator() { }

    /** Equal split. Pennies left over after integer division go to the
     *  participants with the smallest userIds first. */
    public static Map<Long, Long> equal(long totalCents, List<Long> userIds) {
        requirePositive(totalCents);
        requireParticipants(userIds);
        if (userIds.stream().distinct().count() != userIds.size()) {
            throw new BadRequestException("Duplicate participants are not allowed.");
        }
        List<Long> sorted = userIds.stream().sorted().toList();
        long n = sorted.size();
        long base = totalCents / n;
        long remainder = totalCents - base * n; // 0 <= remainder < n
        Map<Long, Long> out = new LinkedHashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            out.put(sorted.get(i), base + (i < remainder ? 1L : 0L));
        }
        return out;
    }

    /** Each participant supplies an exact amount. Sum must equal total. */
    public static Map<Long, Long> exact(long totalCents, Map<Long, BigDecimal> amounts) {
        requirePositive(totalCents);
        if (amounts == null || amounts.isEmpty()) {
            throw new BadRequestException("At least one participant is required.");
        }
        Map<Long, Long> out = new LinkedHashMap<>();
        long sum = 0L;
        for (var entry : amounts.entrySet()) {
            long cents = toScale2Long(entry.getValue(), "Share");
            if (cents < 0) {
                throw new BadRequestException("Shares cannot be negative.");
            }
            out.put(entry.getKey(), cents);
            sum = Math.addExact(sum, cents);
        }
        if (sum != totalCents) {
            BigDecimal got = BigDecimal.valueOf(sum, 2);
            BigDecimal expected = BigDecimal.valueOf(totalCents, 2);
            throw new BadRequestException(
                "Shares sum to " + got + " but total is " + expected + ".");
        }
        return out;
    }

    /** Percentages summing to 100.00. Floor each, then sprinkle the
     *  remainder one-cent at a time to the smallest userIds. */
    public static Map<Long, Long> percent(long totalCents, Map<Long, BigDecimal> percents) {
        requirePositive(totalCents);
        if (percents == null || percents.isEmpty()) {
            throw new BadRequestException("At least one participant is required.");
        }
        Map<Long, Long> hundredths = new LinkedHashMap<>(); // scale-2 long, 33.33% -> 3333
        long sumHundredths = 0L;
        for (var entry : percents.entrySet()) {
            BigDecimal p = entry.getValue();
            if (p == null) {
                throw new BadRequestException("Percent is required for every participant.");
            }
            if (p.signum() < 0) {
                throw new BadRequestException("Percent cannot be negative.");
            }
            long h = toScale2Long(p, "Percent");
            hundredths.put(entry.getKey(), h);
            sumHundredths = Math.addExact(sumHundredths, h);
        }
        if (sumHundredths != PERCENT_HUNDREDTHS) {
            throw new BadRequestException("Percentages sum to "
                + BigDecimal.valueOf(sumHundredths, 2)
                + "% but must total 100.00%.");
        }
        Map<Long, Long> out = new LinkedHashMap<>();
        long allocated = 0L;
        for (var entry : hundredths.entrySet()) {
            long share = Math.multiplyExact(totalCents, entry.getValue()) / PERCENT_HUNDREDTHS;
            out.put(entry.getKey(), share);
            allocated = Math.addExact(allocated, share);
        }
        long remainder = totalCents - allocated;
        if (remainder > 0) {
            List<Long> sortedKeys = out.keySet().stream().sorted().toList();
            for (int i = 0; i < remainder; i++) {
                Long uid = sortedKeys.get((int) (i % sortedKeys.size()));
                out.merge(uid, 1L, Long::sum);
            }
        }
        return out;
    }

    private static void requirePositive(long total) {
        if (total <= 0) {
            throw new BadRequestException("Total amount must be positive.");
        }
    }

    private static void requireParticipants(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BadRequestException("At least one participant is required.");
        }
    }

    private static long toScale2Long(BigDecimal value, String label) {
        if (value == null) {
            throw new BadRequestException(label + " is required.");
        }
        try {
            return value.movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new BadRequestException(label + " " + value + " has more than 2 decimal places.");
        }
    }
}
